package com.agenda.kanban.notification.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Notificación in-app mostrada cuando un {@code Reminder} se dispara (FR-014). POJO puro de
 * dominio — ver plan.md (Arquitectura Hexagonal) y data-model.md § Notification.
 */
public final class Notification {

    private final UUID id;
    private final UUID userId;
    private final UUID reminderId;
    private final String message;
    private final Instant readAt;
    private final Instant createdAt;

    public Notification(UUID id, UUID userId, UUID reminderId, String message, Instant readAt, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        this.reminderId = Objects.requireNonNull(reminderId, "reminderId es obligatorio");
        this.message = validateMessage(message);
        this.readAt = readAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt es obligatorio");
    }

    public static Notification create(UUID id, UUID userId, UUID reminderId, String message, Instant now) {
        return new Notification(id, userId, reminderId, message, null, now);
    }

    private static String validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new ValidationException("El mensaje de la notificación es obligatorio");
        }
        return message;
    }

    public Notification markRead(Instant now) {
        return new Notification(id, userId, reminderId, message, now, createdAt);
    }

    public boolean isRead() {
        return readAt != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getReminderId() {
        return reminderId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
