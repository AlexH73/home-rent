package de.ait.homerent.mail;

import de.ait.homerent.booking.dto.BookingEmailRequest;
import de.ait.homerent.booking.dto.RentalFinishedEmailRequest;
import de.ait.homerent.contract.dto.ContractUploadedEmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 13.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Service
@Slf4j

public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailService(TemplateEngine templateEngine, JavaMailSender javaMailSender) {
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
    }

    @Value("${app.mail.from}")
    private String from;

    // ------------------------ Booking Approved ------------------------
    public void sendBookingApproved(BookingEmailRequest request) {
        Context context = new Context();
        context.setVariable("username", request.getUsername());
        context.setVariable("propertyAddress", request.getPropertyAddress());
        context.setVariable("startDate", request.getStartDate());
        context.setVariable("endDate", request.getEndDate());
        context.setVariable("totalPrice", request.getTotalPrice());
        context.setVariable("confirmUrl", request.getConfirmUrl());

        sendEmail(request.getEmail(), "Booking Confirmation", "mail/booking-confirmation", context);
    }

    // ------------------------ Rental Finished ------------------------
    public void sendRentalFinished(RentalFinishedEmailRequest request) {
        Context context = new Context();
        context.setVariable("username", request.getUsername());
        context.setVariable("propertyAddress", request.getPropertyAddress());
        context.setVariable("startDate", request.getStartDate());
        context.setVariable("endDate", request.getEndDate());
        context.setVariable("totalPrice", request.getTotalPrice());

        sendEmail(request.getEmail(), "Rental Finished", "mail/rental-finished-notice", context);
    }

    // ------------------------ Contract Uploaded ------------------------
    public void sendContractUploaded(ContractUploadedEmailRequest request) {
        Context context = new Context();
        context.setVariable("username", request.getUsername());
        context.setVariable("propertyAddress", request.getPropertyAddress());
        context.setVariable("contractFileName", request.getContractFileName());

        sendEmail(request.getEmail(), "Contract Uploaded", "mail/contract-upload-confirmation", context);
    }

    // ------------------------ Helper method ------------------------
    private void sendEmail(String to, String subject, String templateName, Context context) {
        String htmlContent = templateEngine.process(templateName, context);
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.info("Sending email '{}' to {}", subject, to);
            javaMailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}