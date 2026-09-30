package com.agenda.kanban.task.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "eliminar tarjeta" (DELETE /tasks/{taskId}). */
public interface DeleteTaskUseCase {

    void deleteTask(DeleteTaskCommand command);

    record DeleteTaskCommand(UUID userId, UUID taskId) {
    }
}
