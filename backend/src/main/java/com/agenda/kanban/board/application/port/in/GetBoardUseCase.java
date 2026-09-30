package com.agenda.kanban.board.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "obtener el tablero completo" (GET /workspaces/{workspaceId}/board),
 * con sus columnas ordenadas por posición y las tareas de cada columna.
 */
public interface GetBoardUseCase {

    BoardView getBoard(GetBoardQuery query);

    record GetBoardQuery(UUID userId, UUID workspaceId) {
    }

    record BoardView(UUID boardId, List<ColumnView> columns) {
    }

    record ColumnView(UUID id, String name, int position, List<TaskView> tasks) {
    }

    record TaskView(
            UUID id, String title, String description, boolean completed,
            Instant dueAt, Integer reminderLeadMinutes, boolean overdue) {
    }
}
