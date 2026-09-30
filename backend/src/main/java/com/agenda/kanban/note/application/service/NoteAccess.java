package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.note.domain.model.ChecklistItem;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.UUID;

/**
 * Helper interno del módulo note: resuelve y verifica la propiedad (FR-009) de un espacio de
 * trabajo o de un elemento de checklist para el usuario autenticado. No es un puerto; evita
 * duplicar la comprobación en cada servicio de este módulo.
 */
final class NoteAccess {

    private NoteAccess() {
    }

    static Workspace requireOwnedWorkspace(UUID userId, UUID workspaceId, WorkspaceRepositoryPort workspaceRepository) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(userId)) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }
        return workspace;
    }

    static ChecklistItem requireOwnedItem(UUID userId, UUID itemId, ChecklistItemRepositoryPort itemRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Elemento de checklist no encontrado"));
        requireOwnedWorkspace(userId, item.getWorkspaceId(), workspaceRepository);
        return item;
    }
}
