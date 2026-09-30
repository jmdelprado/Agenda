package com.agenda.kanban.task.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "crear tarjeta" (POST /boards/columns/{columnId}/tasks). */
public interface CreateTaskUseCase {

    TaskResult createTask(CreateTaskCommand command);

    record CreateTaskCommand(UUID userId, UUID columnId, String title, String description) {
    }

    record TaskResult(UUID id, UUID columnId, String title, String description, boolean completed) {
    }
}
