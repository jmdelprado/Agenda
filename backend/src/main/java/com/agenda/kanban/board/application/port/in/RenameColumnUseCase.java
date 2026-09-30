package com.agenda.kanban.board.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "renombrar columna" (PATCH /boards/columns/{columnId}). */
public interface RenameColumnUseCase {

    void renameColumn(RenameColumnCommand command);

    record RenameColumnCommand(UUID userId, UUID columnId, String newName) {
    }
}
