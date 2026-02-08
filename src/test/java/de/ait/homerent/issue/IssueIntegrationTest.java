package de.ait.homerent.issue;

import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IssueIntegrationTest {

    @Autowired
    private IssueReportRepository issueReportRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("pass");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private IssueReport createReport(User user, String description) {
        IssueReport report = new IssueReport();
        report.setReportedBy(user);
        report.setDescription(description);
        report.setPhotoPath("/uploads/issues/" + description.hashCode() + ".jpg");
        report.setStatus(IssueStatus.OPEN);
        return report;
    }

    @Test
    void save_validIssueReport_shouldPersist() {
        User user = createUser("tenant_valid");

        IssueReport report = createReport(user, "Water leak in bathroom");
        IssueReport saved = issueReportRepository.save(report);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(IssueStatus.OPEN);
        assertThat(saved.getReportedBy().getId()).isEqualTo(user.getId());
    }

    @Test
    void save_invalidDescription_shouldThrowValidationError() {
        User user = createUser("tenant_invalid");

        IssueReport report = createReport(user, "");

        assertThatThrownBy(() -> issueReportRepository.saveAndFlush(report))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void creationTimestamp_shouldBeGeneratedAutomatically() {
        User user = createUser("tenant_timestamp");

        IssueReport report = createReport(user, "Broken window");
        IssueReport saved = issueReportRepository.save(report);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
