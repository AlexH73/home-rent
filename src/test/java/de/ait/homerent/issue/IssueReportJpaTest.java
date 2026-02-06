package de.ait.homerent.issue;

import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class IssueReportJpaTest {

    @Autowired
    private IssueReportRepository issueReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_validIssueReport_shouldPersist() {
        User user = new User();
        user.setUsername("tenant2");
        user.setEmail("tenant1@example.com");
        user.setPassword("pass");
        user.setEnabled(true);
        user = userRepository.save(user);

        IssueReport report = new IssueReport();
        report.setReportedBy(user);
        report.setDescription("Water leak in bathroom");
        report.setPhotoPath("/uploads/issues/photo1.jpg");
        report.setStatus(IssueStatus.OPEN);

        IssueReport saved = issueReportRepository.save(report);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(IssueStatus.OPEN);
        assertThat(saved.getReportedBy().getId()).isEqualTo(user.getId());
    }

    @Test
    void save_invalidDescription_shouldFailValidation() {
        User user = new User();
        user.setUsername("tenant2");
        user.setEmail("tenant2@example.com");
        user.setPassword("pass");
        user.setEnabled(true);
        user = userRepository.save(user);

        IssueReport report = new IssueReport();
        report.setReportedBy(user);
        report.setDescription(""); // invalid
        report.setPhotoPath("/uploads/issues/photo2.jpg");
        report.setStatus(IssueStatus.OPEN);

        assertThatThrownBy(() -> issueReportRepository.saveAndFlush(report))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void creationTimestamp_shouldBeGeneratedAutomatically() {
        User user = new User();
        user.setUsername("tenant3");
        user.setEmail("tenant3@example.com");
        user.setPassword("pass");
        user.setEnabled(true);
        user = userRepository.save(user);

        IssueReport report = new IssueReport();
        report.setReportedBy(user);
        report.setDescription("Broken window");
        report.setPhotoPath("/uploads/issues/photo3.jpg");
        report.setStatus(IssueStatus.OPEN);

        IssueReport saved = issueReportRepository.save(report);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}

