package com.agenda.kanban.board.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Tablero, en relación 1:1 con un Workspace. POJO puro de dominio — ver plan.md
 * (Arquitectura Hexagonal). No tiene reglas de negocio propias más allá de su identidad.
 */
public final class Board {

    private final UUID id;
    private final UUID workspaceId;

    public Board(UUID id, UUID workspaceId) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId es obligatorio");
    }

    public static Board create(UUID id, UUID workspaceId) {
        return new Board(id, workspaceId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board board)) return false;
        return id.equals(board.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
