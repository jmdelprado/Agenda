package com.agenda.kanban.note.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Elemento de una checklist rápida de un día concreto — independiente de las tarjetas Kanban
 * (FR de la agenda de papel: "apuntar checklist y demás"). POJO puro de dominio.
 */
public final class ChecklistItem {

    private static final int TEXT_MAX_LENGTH = 500;

    private final UUID id;
    private final UUID workspaceId;
    private final LocalDate date;
    private final String text;
    private final boolean done;
    private final int position;
    private final Instant createdAt;

    public ChecklistItem(UUID id, UUID workspaceId, LocalDate date, String text, boolean done, int position,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId es obligatorio");
        this.date = Objects.requireNonNull(date, "date es obligatorio");
        this.text = validateText(text);
        this.done = done;
        this.position = position;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt es obligatorio");
    }

    public static ChecklistItem create(UUID id, UUID workspaceId, LocalDate date, String text, int position,
            Instant now) {
        return new ChecklistItem(id, workspaceId, date, text, false, position, now);
    }

    private static String validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("El texto del elemento de checklist es obligatorio");
        }
        String trimmed = text.trim();
        if (trimmed.length() > TEXT_MAX_LENGTH) {
            throw new ValidationException(
                    "El texto del elemento de checklist debe tener entre 1 y " + TEXT_MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }

    public ChecklistItem withText(String newText) {
        return new ChecklistItem(id, workspaceId, date, newText, done, position, createdAt);
    }

    public ChecklistItem withDone(boolean newDone) {
        return new ChecklistItem(id, workspaceId, date, text, newDone, position, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }

    public int getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChecklistItem that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
