package com.agenda.kanban.note.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Mapea la tabla {@code day_notes} (una nota de texto libre por espacio de trabajo y día). */
@Entity
@Table(name = "day_notes")
public class DayNoteJpaEntity {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(nullable = false)
    private String content;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DayNoteJpaEntity() {
        // requerido por JPA
    }

    public DayNoteJpaEntity(UUID id, UUID workspaceId, LocalDate noteDate, String content, Instant updatedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.noteDate = noteDate;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LocalDate getNoteDate() {
        return noteDate;
    }

    public String getContent() {
        return content;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
