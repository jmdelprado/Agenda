package com.agenda.kanban.note.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Nota de texto libre de un día concreto dentro de un espacio de trabajo — una por
 * (workspaceId, date). POJO puro de dominio, ver plan.md (Arquitectura Hexagonal).
 */
public final class DayNote {

    private static final int CONTENT_MAX_LENGTH = 5000;

    private final UUID id;
    private final UUID workspaceId;
    private final LocalDate date;
    private final String content;
    private final Instant updatedAt;

    public DayNote(UUID id, UUID workspaceId, LocalDate date, String content, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId es obligatorio");
        this.date = Objects.requireNonNull(date, "date es obligatorio");
        this.content = validateContent(content);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt es obligatorio");
    }

    public static DayNote create(UUID id, UUID workspaceId, LocalDate date, String content, Instant now) {
        return new DayNote(id, workspaceId, date, content, now);
    }

    private static String validateContent(String content) {
        String normalized = content == null ? "" : content;
        if (normalized.length() > CONTENT_MAX_LENGTH) {
            throw new ValidationException("La nota no puede superar " + CONTENT_MAX_LENGTH + " caracteres");
        }
        return normalized;
    }

    public DayNote withContent(String newContent, Instant now) {
        return new DayNote(id, workspaceId, date, newContent, now);
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

    public String getContent() {
        return content;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayNote dayNote)) return false;
        return id.equals(dayNote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
