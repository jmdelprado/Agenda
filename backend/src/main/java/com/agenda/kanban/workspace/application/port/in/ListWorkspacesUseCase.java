package com.agenda.kanban.workspace.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "listar los espacios de trabajo (pestañas) del usuario"
 * (GET /workspaces). Excluye los archivados (eliminados vía soft delete, T064).
 */
public interface ListWorkspacesUseCase {

    List<WorkspaceSummary> listWorkspaces(UUID userId);

    record WorkspaceSummary(UUID id, String name, Instant createdAt) {
    }
}
