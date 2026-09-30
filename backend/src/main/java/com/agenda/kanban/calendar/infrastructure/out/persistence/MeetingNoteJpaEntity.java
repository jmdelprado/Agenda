package com.agenda.kanban.calendar.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Mapea la tabla {@code meeting_notes} (una nota de texto libre por usuario y evento de Google Calendar). */
@Entity
@Table(name = "meeting_notes")
public class MeetingNoteJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String content;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MeetingNoteJpaEntity() {
        // requerido por JPA
    }

    public MeetingNoteJpaEntity(UUID id, UUID userId, String eventId, String content, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.content = content;
        this.updatedAt = updatedAt;
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
}
