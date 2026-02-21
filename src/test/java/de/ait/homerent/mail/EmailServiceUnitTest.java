package de.ait.homerent.mail;

import de.ait.homerent.booking.dto.BookingEmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmailServiceUnitTest {

    private EmailServiceTestProxy emailService;
    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        templateEngine = Mockito.mock(TemplateEngine.class);

        emailService = new EmailServiceTestProxy(mailSender, templateEngine);
    }

    @Test
    void testSendBookingApproved_success() throws Exception {

        // Arrange
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
        TemplateEngine templateEngine = Mockito.mock(TemplateEngine.class);

        EmailServiceTestProxy emailService = new EmailServiceTestProxy(mailSender, templateEngine);

        // From-Adresse setzen (wichtig!)
        Field fromField = EmailService.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(emailService, "noreply@test.com");

        BookingEmailRequest request = new BookingEmailRequest();
        request.setEmail("test@example.com");
        request.setUsername("John");
        request.setPropertyAddress("Teststraße 1");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTotalPrice(500);
        request.setConfirmUrl("http://localhost/confirm");

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email OK</html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendBookingApproved(request);

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }


    @Test
    void testSendBookingApproved_mailSenderFails() throws Exception {
        // Arrange
        BookingEmailRequest request = new BookingEmailRequest();
        request.setEmail("test@example.com");
        request.setUsername("John");
        request.setPropertyAddress("Teststraße 1");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTotalPrice(500);
        request.setConfirmUrl("http://localhost/confirm");

        // From-Adresse setzen (wichtig!)
        Field fromField = EmailService.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(emailService, "noreply@test.com");

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Error</html>");

        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("Mail server down"));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> emailService.sendBookingApproved(request));
    }

}
