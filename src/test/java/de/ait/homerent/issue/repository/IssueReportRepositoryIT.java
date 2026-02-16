package de.ait.homerent.issue.repository;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Integration tests for IssueReportRepository")
class IssueReportRepositoryIT {

    @Autowired
    private IssueReportRepository issueReportRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testTenant;
    private User testOwner;
    private Property testProperty;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_TENANT).build()));
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_OWNER).build()));

        // Creating a test tenant
        testTenant = new User();
        testTenant.setUsername("testtenant");
        testTenant.setEmail("tenant@test.com");
        testTenant.setPassword("pass");
        testTenant.setEnabled(true);
        testTenant.setRoles(Set.of(tenantRole));
        userRepository.save(testTenant);

        // Creating a test owner
        testOwner = new User();
        testOwner.setUsername("testowner");
        testOwner.setEmail("owner@test.com");
        testOwner.setPassword("pass");
        testOwner.setEnabled(true);
        testOwner.setRoles(Set.of(ownerRole));
        userRepository.save(testOwner);

        // Creating a test property
        testProperty = new Property();
        testProperty.setOwner(testOwner);
        testProperty.setTitle("Test Property");
        testProperty.setAddress("Test Address");
        testProperty.setDescription("Test Description");
        testProperty.setPricePerDay(1000);
        testProperty.setStatus(PropertyStatus.AVAILABLE);
        testProperty.setAvailableFrom(LocalDateTime.now());
        testProperty.setAvailableTo(LocalDateTime.now().plusMonths(6));
        propertyRepository.save(testProperty);

        // Creating a test booking
        testBooking = new Booking();
        testBooking.setProperty(testProperty);
        testBooking.setTenant(testTenant);
        testBooking.setStartDate(LocalDateTime.now().plusDays(1));
        testBooking.setEndDate(LocalDateTime.now().plusDays(5));
        testBooking.setStatus(BookingStatus.ACTIVE);
        testBooking.setTotalPrice(4000);
        bookingRepository.save(testBooking);
    }

    @Test
    @DisplayName("Should save and find issue report by ID")
    void saveAndFindById() {
        // given
        IssueReport issue = new IssueReport();
        issue.setBooking(testBooking);
        issue.setReportedBy(testTenant);
        issue.setDescription("Leaking faucet");
        issue.setPhotoPath("issues/2026-02-15/faucet.jpg");
        issue.setStatus(IssueStatus.OPEN);
        // createdAt will be initialized automatically.

        // when
        IssueReport saved = issueReportRepository.save(issue);
        entityManager.flush();
        entityManager.clear();

        Optional<IssueReport> found = issueReportRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        IssueReport loaded = found.get();
        assertThat(loaded.getId()).isNotNull();
        assertThat(loaded.getDescription()).isEqualTo("Leaking faucet");
        assertThat(loaded.getPhotoPath()).isEqualTo("issues/2026-02-15/faucet.jpg");
        assertThat(loaded.getStatus()).isEqualTo(IssueStatus.OPEN);
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getBooking().getId()).isEqualTo(testBooking.getId());
        assertThat(loaded.getReportedBy().getId()).isEqualTo(testTenant.getId());
    }

    @Test
    @DisplayName("Should find issues by status")
    void findByStatus() {
        issueReportRepository.deleteAll();
        entityManager.flush();

        // given
        IssueReport issue1 = new IssueReport();
        issue1.setBooking(testBooking);
        issue1.setReportedBy(testTenant);
        issue1.setDescription("Issue 1");
        issue1.setPhotoPath("path1.jpg");
        issue1.setStatus(IssueStatus.OPEN);

        IssueReport issue2 = new IssueReport();
        issue2.setBooking(testBooking);
        issue2.setReportedBy(testTenant);
        issue2.setDescription("Issue 2");
        issue2.setPhotoPath("path2.jpg");
        issue2.setStatus(IssueStatus.IN_PROGRESS);

        IssueReport issue3 = new IssueReport();
        issue3.setBooking(testBooking);
        issue3.setReportedBy(testTenant);
        issue3.setDescription("Issue 3");
        issue3.setPhotoPath("path3.jpg");
        issue3.setStatus(IssueStatus.OPEN);

        issueReportRepository.saveAll(List.of(issue1, issue2, issue3));
        entityManager.flush();
        entityManager.clear();

        // when
        List<IssueReport> openIssues = issueReportRepository.findByStatus(IssueStatus.OPEN);

        // then
        assertThat(openIssues).hasSize(2);
        assertThat(openIssues).extracting(IssueReport::getDescription)
                .containsExactlyInAnyOrder("Issue 1", "Issue 3");
    }

    @Test
    @DisplayName("Should find issues by reported user ID")
    void findByReportedById() {
        // given
        IssueReport issue1 = new IssueReport();
        issue1.setBooking(testBooking);
        issue1.setReportedBy(testTenant);
        issue1.setDescription("Issue by tenant");
        issue1.setPhotoPath("path1.jpg");
        issue1.setStatus(IssueStatus.OPEN);

        IssueReport issue2 = new IssueReport();
        issue2.setBooking(testBooking);
        issue2.setReportedBy(testTenant);
        issue2.setDescription("Another issue by tenant");
        issue2.setPhotoPath("path2.jpg");
        issue2.setStatus(IssueStatus.IN_PROGRESS);

        issueReportRepository.saveAll(List.of(issue1, issue2));
        entityManager.flush();

        // when
        List<IssueReport> found = issueReportRepository.findByReportedById(testTenant.getId());

        // then
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(issue -> issue.getReportedBy().getId().equals(testTenant.getId()));
    }

    @Test
    @DisplayName("Should not allow null booking (DB constraint)")
    void notNullBooking() {
        IssueReport issue = new IssueReport();
        issue.setBooking(null);
        issue.setReportedBy(testTenant);
        issue.setDescription("Valid description");
        issue.setPhotoPath("path.jpg");
        issue.setStatus(IssueStatus.OPEN);

        assertThrows(DataIntegrityViolationException.class, () -> {
            issueReportRepository.save(issue);
            issueReportRepository.flush();
        });
    }

    @Test
    @DisplayName("Should not allow null reportedBy (DB constraint)")
    void notNullReportedBy() {
        IssueReport issue = new IssueReport();
        issue.setBooking(testBooking);
        issue.setReportedBy(null);
        issue.setDescription("Valid description");
        issue.setPhotoPath("path.jpg");
        issue.setStatus(IssueStatus.OPEN);

        assertThrows(DataIntegrityViolationException.class, () -> {
            issueReportRepository.save(issue);
            issueReportRepository.flush();
        });
    }

    @Test
    @DisplayName("Should not allow null description (validation)")
    void notNullDescription() {
        IssueReport issue = new IssueReport();
        issue.setBooking(testBooking);
        issue.setReportedBy(testTenant);
        issue.setDescription(null);
        issue.setPhotoPath("path.jpg");
        issue.setStatus(IssueStatus.OPEN);

        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            issueReportRepository.save(issue);
            issueReportRepository.flush();
        });
    }

    @Test
    @DisplayName("Should not allow null photoPath (DB constraint)")
    void notNullPhotoPath() {
        IssueReport issue = new IssueReport();
        issue.setBooking(testBooking);
        issue.setReportedBy(testTenant);
        issue.setDescription("Valid description");
        issue.setPhotoPath(null);
        issue.setStatus(IssueStatus.OPEN);

        assertThrows(DataIntegrityViolationException.class, () -> {
            issueReportRepository.save(issue);
            issueReportRepository.flush();
        });
    }

    @Test
    @DisplayName("Should not allow null status (DB constraint)")
    void notNullStatus() {
        IssueReport issue = new IssueReport();
        issue.setBooking(testBooking);
        issue.setReportedBy(testTenant);
        issue.setDescription("Valid description");
        issue.setPhotoPath("path.jpg");

        assertThrows(DataIntegrityViolationException.class, () -> {
            issueReportRepository.save(issue);
            issueReportRepository.flush();
        });
    }
}
