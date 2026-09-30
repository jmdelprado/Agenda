package com.agenda.kanban.reminder.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Recordatorio asociado a la fecha límite de una tarea (FR-005, FR-006, FR-014). POJO puro de
 * dominio — ver plan.md (Arquitectura Hexagonal) y data-model.md § Reminder para las transiciones
 * de estado: PENDING → SENT / CANCELLED / FAILED.
 */
public final class Reminder {

    public enum Status { PENDING, SENT, CANCELLED, FAILED }

    public enum Channel { IN_APP, EMAIL }

    private final UUID id;
    private final UUID taskId;
    private final Instant triggerAt;
    private final Status status;
    private final Set<Channel> channels;
    private final Instant sentAt;

    public Reminder(UUID id, UUID taskId, Instant triggerAt, Status status, Set<Channel> channels, Instant sentAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.taskId = Objects.requireNonNull(taskId, "taskId es obligatorio");
        this.triggerAt = Objects.requireNonNull(triggerAt, "triggerAt es obligatorio");
        this.status = Objects.requireNonNull(status, "status es obligatorio");
        if (channels == null || channels.isEmpty()) {
            throw new ValidationException("El recordatorio debe tener al menos un canal de notificación");
        }
        this.channels = EnumSet.copyOf(channels);
        this.sentAt = sentAt;
    }

    /** Crea un recordatorio PENDING con los canales por defecto (FR-014: in-app + email). */
    public static Reminder schedule(UUID id, UUID taskId, Instant triggerAt) {
        return new Reminder(id, taskId, triggerAt, Status.PENDING, EnumSet.of(Channel.IN_APP, Channel.EMAIL), null);
    }

    /** Reprograma el disparo de un recordatorio aún pendiente (p.ej. al cambiar dueAt/antelación). */
    public Reminder reschedule(Instant newTriggerAt) {
        if (status != Status.PENDING) {
            throw new ValidationException("Solo se puede reprogramar un recordatorio pendiente");
        }
        return new Reminder(id, taskId, newTriggerAt, Status.PENDING, channels, null);
    }

    /** Cancela el recordatorio (p.ej. al eliminar/cambiar dueAt o al archivar su workspace). No-op si ya se envió. */
    public Reminder cancel() {
        if (status == Status.SENT) {
            return this;
        }
        return new Reminder(id, taskId, triggerAt, Status.CANCELLED, channels, sentAt);
    }

    public Reminder markSent(Instant sentAt) {
        return new Reminder(id, taskId, triggerAt, Status.SENT, channels, sentAt);
    }

    public Reminder markFailed() {
        return new Reminder(id, taskId, triggerAt, Status.FAILED, channels, sentAt);
    }

    /** true si sigue pendiente y su triggerAt ya se alcanzó (procesable por el job de disparo). */
    public boolean isDue(Instant now) {
        return status == Status.PENDING && !triggerAt.isAfter(now);
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

    public Status getStatus() {
        return status;
    }

    public Set<Channel> getChannels() {
        return EnumSet.copyOf(channels);
    }

    public Instant getSentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reminder reminder)) return false;
        return id.equals(reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
