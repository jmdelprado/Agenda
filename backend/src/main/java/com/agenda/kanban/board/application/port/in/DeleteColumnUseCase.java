package com.agenda.kanban.board.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "eliminar columna" (DELETE /boards/columns/{columnId}).
 * Rechaza la operación (ConflictException, 409) si es la única columna del tablero.
 * Las tareas de la columna se eliminan en cascada a nivel de base de datos (ON DELETE CASCADE).
 */
public interface DeleteColumnUseCase {

    void deleteColumn(DeleteColumnCommand command);

    record DeleteColumnCommand(UUID userId, UUID columnId) {
    }
}
