package com.agenda.kanban.task.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Tarjeta / tarea (FR-001, FR-003, FR-006, FR-007). POJO puro de dominio — ver plan.md
 * (Arquitectura Hexagonal). User Story 2 (T047) añade la fecha límite opcional, la antelación
 * de recordatorio (con su valor por defecto) y el estado derivado "vencida".
 */
public final class Task {

    private static final int TITLE_MAX_LENGTH = 200;

    /** Antelación por defecto del recordatorio cuando se asigna dueAt sin indicar una explícita (FR-006). */
    public static final int DEFAULT_REMINDER_LEAD_MINUTES = 1440;

    private final UUID id;
    private final UUID columnId;
    private final String title;
    private final String description;
    private final Instant dueAt;
    private final Integer reminderLeadMinutes;
    private final Instant completedAt;

    public Task(UUID id, UUID columnId, String title, String description, Instant dueAt,
            Integer reminderLeadMinutes, Instant completedAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.columnId = Objects.requireNonNull(columnId, "columnId es obligatorio");
        this.title = validateTitle(title);
        this.description = description;
        this.dueAt = dueAt;
        this.reminderLeadMinutes = normalizeReminderLeadMinutes(dueAt, reminderLeadMinutes);
        this.completedAt = completedAt;
    }

    public static Task create(UUID id, UUID columnId, String title, String description) {
        return new Task(id, columnId, title, description, null, null, null);
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("El título de la tarea es obligatorio");
        }
        String trimmed = title.trim();
        if (trimmed.length() > TITLE_MAX_LENGTH) {
            throw new ValidationException(
                    "El título de la tarea debe tener entre 1 y " + TITLE_MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }

    /**
     * Si {@code dueAt} está definido y no se indica una antelación explícita, se usa el valor por
     * defecto (24h = {@value #DEFAULT_REMINDER_LEAD_MINUTES} min); sin dueAt no hay antelación.
     */
    private static Integer normalizeReminderLeadMinutes(Instant dueAt, Integer reminderLeadMinutes) {
        if (dueAt == null) {
            return null;
        }
        if (reminderLeadMinutes == null) {
            return DEFAULT_REMINDER_LEAD_MINUTES;
        }
        if (reminderLeadMinutes < 0) {
            throw new ValidationException("La antelación del recordatorio no puede ser negativa");
        }
        return reminderLeadMinutes;
    }

    public Task edit(String newTitle, String newDescription) {
        return new Task(id, columnId, newTitle, newDescription, dueAt, reminderLeadMinutes, completedAt);
    }

    /** Mueve la tarea a otra columna. {@code completing} decide si se marca como completada. */
    public Task moveTo(UUID newColumnId, boolean completing, Instant now) {
        Instant newCompletedAt = completing ? now : null;
        return new Task(id, newColumnId, title, description, dueAt, reminderLeadMinutes, newCompletedAt);
    }

    /**
     * Asigna, modifica o elimina (pasando {@code newDueAt = null}) la fecha límite y su antelación
     * de recordatorio (US2-AS1, US2-AS4).
     */
    public Task withDueDate(Instant newDueAt, Integer newReminderLeadMinutes) {
        return new Task(id, columnId, title, description, newDueAt, newReminderLeadMinutes, completedAt);
    }

    /** FR-007: vencida = tiene fecha límite, ya pasó, y no está completada. */
    public boolean isOverdue(Instant now) {
        return dueAt != null && dueAt.isBefore(now) && completedAt == null;
    }

    /** triggerAt del recordatorio = dueAt - reminderLeadMinutes (data-model.md § Reminder); null si no hay dueAt. */
    public Instant computeReminderTriggerAt() {
        if (dueAt == null) {
            return null;
        }
        return dueAt.minus(Duration.ofMinutes(reminderLeadMinutes));
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getColumnId() {
        return columnId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Integer getReminderLeadMinutes() {
        return reminderLeadMinutes;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
