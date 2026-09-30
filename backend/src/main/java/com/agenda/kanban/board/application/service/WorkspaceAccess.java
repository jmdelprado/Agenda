package com.agenda.kanban.board.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.UUID;

/**
 * Helper interno del módulo board: resuelve y verifica la propiedad (FR-009) de un workspace,
 * o de un board/columna a través de su workspace, para el usuario autenticado.
 * No es un puerto (no se expone fuera de application/service); evita duplicar el recorrido
 * columna → board → workspace en cada servicio de este módulo.
 */
final class WorkspaceAccess {

    private WorkspaceAccess() {
    }

    static Workspace requireOwnedWorkspace(UUID userId, UUID workspaceId, WorkspaceRepositoryPort workspaceRepository) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        requireOwnership(userId, workspace);
        return workspace;
    }

    static Board requireOwnedBoard(UUID userId, UUID workspaceId, BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        requireOwnedWorkspace(userId, workspaceId, workspaceRepository);
        return boardRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new NotFoundException("Tablero no encontrado"));
    }

    static Column requireOwnedColumn(UUID userId, UUID columnId, ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository, WorkspaceRepositoryPort workspaceRepository) {
        Column column = columnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("Columna no encontrada"));
        Board board = boardRepository.findById(column.getBoardId())
                .orElseThrow(() -> new NotFoundException("Tablero no encontrado"));
        requireOwnedWorkspace(userId, board.getWorkspaceId(), workspaceRepository);
        return column;
    }

    private static void requireOwnership(UUID userId, Workspace workspace) {
        if (!workspace.belongsTo(userId)) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }
    }
}
