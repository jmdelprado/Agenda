package com.agenda.kanban.note.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Mapea la tabla {@code checklist_items} (varios elementos por espacio de trabajo y día). */
@Entity
@Table(name = "checklist_items")
public class ChecklistItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private boolean done;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ChecklistItemJpaEntity() {
        // requerido por JPA
    }

    public ChecklistItemJpaEntity(UUID id, UUID workspaceId, LocalDate itemDate, String text, boolean done,
            int position, Instant createdAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.itemDate = itemDate;
        this.text = text;
        this.done = done;
        this.position = position;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LocalDate getItemDate() {
        return itemDate;
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
}
