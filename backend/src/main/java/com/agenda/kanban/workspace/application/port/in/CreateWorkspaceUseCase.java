package com.agenda.kanban.workspace.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "crear espacio de trabajo" (POST /workspaces). Crea, de forma
 * atómica, el Workspace, su Board y las 3 columnas por defecto ("Por hacer"/"En progreso"/"Hecho")
 * — ver data-model.md § Workspace, FR-008.
 */
public interface CreateWorkspaceUseCase {

    CreateWorkspaceResult createWorkspace(CreateWorkspaceCommand command);

    record CreateWorkspaceCommand(UUID userId, String name) {
    }

    record CreateWorkspaceResult(
            UUID workspaceId, String name, Instant createdAt, UUID boardId, List<ColumnSummary> columns) {
    }

    record ColumnSummary(UUID id, String name, int position) {
    }
}
