package com.agenda.kanban.board.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "crear columna" (POST /workspaces/{workspaceId}/board/columns). */
public interface CreateColumnUseCase {

    ColumnResult createColumn(CreateColumnCommand command);

    record CreateColumnCommand(UUID userId, UUID workspaceId, String name) {
    }

    record ColumnResult(UUID id, String name, int position) {
    }
}
