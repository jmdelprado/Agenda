package com.agenda.kanban.task.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Mapea la tabla {@code tasks}, incluyendo {@code due_at}/{@code reminder_lead_minutes}
 * (User Story 2, FR-003/FR-006) además de los campos base de User Story 1 y {@code createdAt}
 * (necesaria para ordenar las tarjetas de una columna).
 */
@Entity
@Table(name = "tasks")
public class TaskJpaEntity {

    @Id
    private UUID id;

    @Column(name = "column_id", nullable = false)
    private UUID columnId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column
    private String description;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "reminder_lead_minutes")
    private Integer reminderLeadMinutes;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TaskJpaEntity() {
        // requerido por JPA
    }

    public TaskJpaEntity(UUID id, UUID columnId, String title, String description, Instant dueAt,
            Integer reminderLeadMinutes, Instant completedAt, Instant createdAt) {
        this.id = id;
        this.columnId = columnId;
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.reminderLeadMinutes = reminderLeadMinutes;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
