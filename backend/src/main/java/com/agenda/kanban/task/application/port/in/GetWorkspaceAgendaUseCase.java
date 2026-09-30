package com.agenda.kanban.task.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "consultar la agenda de un espacio de trabajo"
 * (GET /workspaces/{workspaceId}/agenda, FR-004). Devuelve las tareas con {@code dueAt} definido
 * de ese espacio, ordenadas por fecha límite ascendente.
 */
public interface GetWorkspaceAgendaUseCase {

    List<AgendaTaskResult> getAgenda(GetWorkspaceAgendaQuery query);

    record GetWorkspaceAgendaQuery(UUID userId, UUID workspaceId) {
    }

    record AgendaTaskResult(UUID taskId, String title, Instant dueAt, boolean overdue, boolean completed) {
    }
}
