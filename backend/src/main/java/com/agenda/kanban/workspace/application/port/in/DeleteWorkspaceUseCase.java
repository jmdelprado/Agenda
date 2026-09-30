package com.agenda.kanban.workspace.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "eliminar (soft delete) un espacio de trabajo" (DELETE /workspaces/{id}, FR-012).
 * Archiva el workspace y cancela los recordatorios PENDING de todas sus tareas.
 */
public interface DeleteWorkspaceUseCase {

    void delete(UUID userId, UUID workspaceId);
}
