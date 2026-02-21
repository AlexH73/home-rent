package de.ait.homerent.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

public class EmailServiceTestProxy extends EmailService {

    private final JavaMailSender mailSenderMock;
    private final TemplateEngine templateEngineMock;

    public EmailServiceTestProxy(JavaMailSender mailSenderMock, TemplateEngine templateEngineMock) {
        super(templateEngineMock, mailSenderMock);
        this.mailSenderMock = mailSenderMock;
        this.templateEngineMock = templateEngineMock;
    }

    public JavaMailSender getMailSenderMock() {
        return mailSenderMock;
    }

    public TemplateEngine getTemplateEngineMock() {
        return templateEngineMock;
    }
}

