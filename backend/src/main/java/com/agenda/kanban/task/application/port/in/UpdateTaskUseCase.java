package com.agenda.kanban.task.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "editar tarjeta" (PATCH /tasks/{taskId}). */
public interface UpdateTaskUseCase {

    void updateTask(UpdateTaskCommand command);

    record UpdateTaskCommand(UUID userId, UUID taskId, String title, String description) {
    }
}
