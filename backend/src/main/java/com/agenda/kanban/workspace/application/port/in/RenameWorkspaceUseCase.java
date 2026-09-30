package com.agenda.kanban.workspace.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "renombrar un espacio de trabajo" (PATCH /workspaces/{id}). */
public interface RenameWorkspaceUseCase {

    RenameWorkspaceResult rename(RenameWorkspaceCommand command);

    record RenameWorkspaceCommand(UUID userId, UUID workspaceId, String newName) {
    }

    record RenameWorkspaceResult(UUID workspaceId, String name) {
    }
}
