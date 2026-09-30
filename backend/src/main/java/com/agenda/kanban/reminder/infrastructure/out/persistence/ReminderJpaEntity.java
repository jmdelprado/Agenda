package com.agenda.kanban.reminder.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Mapea la tabla {@code reminders} (ver V1__init.sql / data-model.md § Reminder). */
@Entity
@Table(name = "reminders")
public class ReminderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "trigger_at", nullable = false)
    private Instant triggerAt;

    @Column(nullable = false, length = 20)
    private String status;

    /** Lista separada por comas de {@code Reminder.Channel} (p.ej. "IN_APP,EMAIL"). */
    @Column(nullable = false, length = 50)
    private String channels;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected ReminderJpaEntity() {
        // requerido por JPA
    }

    public ReminderJpaEntity(UUID id, UUID taskId, Instant triggerAt, String status, String channels,
            Instant sentAt) {
        this.id = id;
        this.taskId = taskId;
        this.triggerAt = triggerAt;
        this.status = status;
        this.channels = channels;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public Instant getTriggerAt() {
        return triggerAt;
    }

    public String getStatus() {
        return status;
    }

    public String getChannels() {
        return channels;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
