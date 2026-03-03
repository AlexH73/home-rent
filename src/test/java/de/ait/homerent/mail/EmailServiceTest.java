package de.ait.homerent.mail;

import de.ait.homerent.booking.dto.BookingEmailRequest;
import de.ait.homerent.booking.dto.RentalFinishedEmailRequest;
import de.ait.homerent.contract.dto.ContractUploadedEmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DisplayName("EmailService unit tests")
class EmailServiceTest {

    @Test
    @DisplayName("sendBookingApproved(): renders template and sends message via JavaMailSender")
    void sendBookingApproved_sendsEmail() {
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("mail/booking-confirmation"), any(Context.class)))
                .thenReturn("<html>ok</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService service = new EmailService(templateEngine, mailSender);
        ReflectionTestUtils.setField(service, "from", "noreply@test.com");

        BookingEmailRequest req = new BookingEmailRequest();
        req.setEmail("u@test.com");
        req.setUsername("u1");
        req.setPropertyAddress("addr");
        req.setStartDate(java.time.LocalDate.now());
        req.setEndDate(java.time.LocalDate.now().plusDays(1));
        req.setTotalPrice(100);
        req.setConfirmUrl("http://confirm");

        service.sendBookingApproved(req);

        verify(templateEngine).process(eq("mail/booking-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendBookingApproved(): when JavaMailSender throws, propagates RuntimeException")
    void sendBookingApproved_whenSendThrows_propagates() {
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("mail/booking-confirmation"), any(Context.class)))
                .thenReturn("<html>ok</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(mimeMessage);

        EmailService service = new EmailService(templateEngine, mailSender);
        ReflectionTestUtils.setField(service, "from", "noreply@test.com");

        BookingEmailRequest req = new BookingEmailRequest();
        req.setEmail("u@test.com");
        req.setUsername("u1");
        req.setPropertyAddress("addr");
        req.setStartDate(java.time.LocalDate.now());
        req.setEndDate(java.time.LocalDate.now().plusDays(1));
        req.setTotalPrice(100);
        req.setConfirmUrl("http://confirm");

        assertThatThrownBy(() -> service.sendBookingApproved(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP down");
    }

    @Test
    @DisplayName("sendRentalFinished(): renders template and sends email")
    void sendRentalFinished_sendsEmail() {
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("mail/rental-finished-notice"), any(Context.class)))
                .thenReturn("<html>ok</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService service = new EmailService(templateEngine, mailSender);
        ReflectionTestUtils.setField(service, "from", "noreply@test.com");

        RentalFinishedEmailRequest req = new RentalFinishedEmailRequest();
        req.setEmail("u@test.com");
        req.setUsername("u1");
        req.setPropertyAddress("addr");
        req.setStartDate(java.time.LocalDate.now().minusDays(2));
        req.setEndDate(java.time.LocalDate.now().minusDays(1));
        req.setTotalPrice(100);

        service.sendRentalFinished(req);

        verify(templateEngine).process(eq("mail/rental-finished-notice"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendContractUploaded(): renders template and sends email")
    void sendContractUploaded_sendsEmail() {
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("mail/contract-upload-confirmation"), any(Context.class)))
                .thenReturn("<html>ok</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService service = new EmailService(templateEngine, mailSender);
        ReflectionTestUtils.setField(service, "from", "noreply@test.com");

        ContractUploadedEmailRequest req = new ContractUploadedEmailRequest();
        req.setEmail("u@test.com");
        req.setUsername("u1");
        req.setPropertyAddress("addr");
        req.setContractFileName("contract.pdf");

        service.sendContractUploaded(req);

        verify(templateEngine).process(eq("mail/contract-upload-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }
}