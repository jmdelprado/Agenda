package com.agenda.kanban.notification.infrastructure.out.mail;

import com.agenda.kanban.notification.application.port.out.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Adaptador de salida: envía el email de recordatorio (FR-014) usando el {@link JavaMailSender} de MailConfig. */
@Component
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailSenderAdapter(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@kanban-agenda.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
