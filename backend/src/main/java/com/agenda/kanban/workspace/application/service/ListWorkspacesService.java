package com.agenda.kanban.workspace.application.service;

import com.agenda.kanban.workspace.application.port.in.ListWorkspacesUseCase;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link ListWorkspacesUseCase}. */
@Service
public class ListWorkspacesService implements ListWorkspacesUseCase {

    private final WorkspaceRepositoryPort workspaceRepository;

    public ListWorkspacesService(WorkspaceRepositoryPort workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceSummary> listWorkspaces(UUID userId) {
        return workspaceRepository.findAllByUserId(userId).stream()
                .filter(workspace -> !workspace.isArchived())
                .sorted(Comparator.comparing(Workspace::getCreatedAt))
                .map(workspace -> new WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getCreatedAt()))
                .toList();
    }
}
