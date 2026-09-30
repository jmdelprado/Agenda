package com.agenda.kanban.notification.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Mapea la tabla {@code notifications} (ver V1__init.sql / data-model.md § Notification). */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reminder_id", nullable = false)
    private UUID reminderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected NotificationJpaEntity() {
        // requerido por JPA
    }

    public NotificationJpaEntity(UUID id, UUID userId, UUID reminderId, String message, Instant readAt,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.reminderId = reminderId;
        this.message = message;
        this.readAt = readAt;
        this.createdAt = createdAt;
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
}
