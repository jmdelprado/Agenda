package com.agenda.kanban.workspace.application.service;

import com.agenda.kanban.shared.ConflictException;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.workspace.application.port.in.RenameWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link RenameWorkspaceUseCase}. */
@Service
public class RenameWorkspaceService implements RenameWorkspaceUseCase {

    private final WorkspaceRepositoryPort workspaceRepository;

    public RenameWorkspaceService(WorkspaceRepositoryPort workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public RenameWorkspaceResult rename(RenameWorkspaceCommand command) {
        Workspace workspace = workspaceRepository.findById(command.workspaceId())
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(command.userId())) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }

        Workspace renamed = workspace.rename(command.newName());

        boolean nameTakenByAnotherActiveWorkspace = workspaceRepository.findAllByUserId(command.userId()).stream()
                .filter(other -> !other.getId().equals(workspace.getId()))
                .filter(other -> !other.isArchived())
                .anyMatch(other -> other.getName().equalsIgnoreCase(renamed.getName()));
        if (nameTakenByAnotherActiveWorkspace) {
            throw new ConflictException(
                    "Ya existe un espacio de trabajo con el nombre \"" + renamed.getName() + "\"");
        }

        Workspace saved = workspaceRepository.save(renamed);
        return new RenameWorkspaceResult(saved.getId(), saved.getName());
    }
}
