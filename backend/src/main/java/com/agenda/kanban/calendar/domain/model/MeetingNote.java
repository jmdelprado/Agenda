package com.agenda.kanban.calendar.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Nota de texto libre asociada a un evento concreto de Google Calendar — una por (userId, eventId). */
public final class MeetingNote {

    private static final int CONTENT_MAX_LENGTH = 5000;

    private final UUID id;
    private final UUID userId;
    private final String eventId;
    private final String content;
    private final Instant updatedAt;

    public MeetingNote(UUID id, UUID userId, String eventId, String content, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        this.eventId = Objects.requireNonNull(eventId, "eventId es obligatorio");
        this.content = validateContent(content);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt es obligatorio");
    }

    public static MeetingNote create(UUID userId, String eventId, String content, Instant now) {
        return new MeetingNote(UUID.randomUUID(), userId, eventId, content, now);
    }

    private static String validateContent(String content) {
        String normalized = content == null ? "" : content;
        if (normalized.length() > CONTENT_MAX_LENGTH) {
            throw new ValidationException("La nota no puede superar " + CONTENT_MAX_LENGTH + " caracteres");
        }
        return normalized;
    }

    public MeetingNote withContent(String newContent, Instant now) {
        return new MeetingNote(id, userId, eventId, newContent, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
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
        if (!(o instanceof MeetingNote that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
