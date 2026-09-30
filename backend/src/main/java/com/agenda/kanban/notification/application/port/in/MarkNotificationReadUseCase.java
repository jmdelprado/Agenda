package com.agenda.kanban.notification.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** Puerto de entrada: marcar una notificación in-app como leída (PATCH /notifications/{id}/read). */
public interface MarkNotificationReadUseCase {

    NotificationReadResult markAsRead(MarkNotificationReadCommand command);

    record MarkNotificationReadCommand(UUID userId, UUID notificationId) {
    }

    record NotificationReadResult(UUID id, Instant readAt) {
    }
}
