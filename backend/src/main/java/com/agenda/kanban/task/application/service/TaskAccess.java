package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.UUID;

/**
 * Helper interno del módulo task: resuelve y verifica la propiedad (FR-009) de una tarea o
 * columna a través de columna → board → workspace, para el usuario autenticado.
 * No es un puerto; evita duplicar el recorrido en cada servicio de este módulo.
 */
final class TaskAccess {

    private TaskAccess() {
    }

    static Column requireOwnedColumn(UUID userId, UUID columnId, ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository, WorkspaceRepositoryPort workspaceRepository) {
        Column column = columnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("Columna no encontrada"));
        Board board = boardRepository.findById(column.getBoardId())
                .orElseThrow(() -> new NotFoundException("Tablero no encontrado"));
        Workspace workspace = workspaceRepository.findById(board.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        requireOwnership(userId, workspace);
        return column;
    }

    static Task requireOwnedTask(UUID userId, UUID taskId, TaskRepositoryPort taskRepository,
            ColumnRepositoryPort columnRepository, BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        requireOwnedColumn(userId, task.getColumnId(), columnRepository, boardRepository, workspaceRepository);
        return task;
    }

    private static void requireOwnership(UUID userId, Workspace workspace) {
        if (!workspace.belongsTo(userId)) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }
    }
}
