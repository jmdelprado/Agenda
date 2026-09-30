package com.agenda.kanban.board.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "boards")
public class BoardJpaEntity {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    protected BoardJpaEntity() {
        // requerido por JPA
    }

    public BoardJpaEntity(UUID id, UUID workspaceId) {
        this.id = id;
        this.workspaceId = workspaceId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }
}
