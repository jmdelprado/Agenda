package com.agenda.kanban.task.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "mover tarjeta a otra columna" (PATCH /tasks/{taskId}/move,
 * SC-002). Si la columna destino es la de mayor posición del tablero (columna terminal, p.ej.
 * "Hecho"), la tarea se marca como completada; en caso contrario se reabre.
 */
public interface MoveTaskUseCase {

    void moveTask(MoveTaskCommand command);

    record MoveTaskCommand(UUID userId, UUID taskId, UUID targetColumnId) {
    }
}
