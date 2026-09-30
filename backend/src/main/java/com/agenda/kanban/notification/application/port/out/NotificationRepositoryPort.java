package com.agenda.kanban.notification.application.port.out;

import com.agenda.kanban.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Notification}. Implementado en infrastructure/out/persistence. */
public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    /** Notificaciones del usuario, más recientes primero; {@code unreadOnly} filtra las no leídas. */
    List<Notification> findByUserId(UUID userId, boolean unreadOnly);
}
