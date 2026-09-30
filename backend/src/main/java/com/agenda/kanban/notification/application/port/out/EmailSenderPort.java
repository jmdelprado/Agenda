package com.agenda.kanban.notification.application.port.out;

/** Puerto de salida: envío de email (FR-014). Implementado por un adaptador SMTP en infrastructure/out/mail. */
public interface EmailSenderPort {

    void send(String toEmail, String subject, String body);
}
