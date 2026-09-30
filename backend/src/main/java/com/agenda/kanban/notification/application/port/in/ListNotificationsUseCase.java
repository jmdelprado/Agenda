package com.agenda.kanban.notification.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: listar las notificaciones in-app del usuario (GET /notifications, FR-014). */
public interface ListNotificationsUseCase {

    List<NotificationResult> listNotifications(ListNotificationsQuery query);

    record ListNotificationsQuery(UUID userId, boolean unreadOnly) {
    }

    record NotificationResult(UUID id, String message, UUID reminderId, Instant readAt, Instant createdAt) {
    }
}
