package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.GetChecklistUseCase;
import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link GetChecklistUseCase}. */
@Service
public class GetChecklistService implements GetChecklistUseCase {

    private final ChecklistItemRepositoryPort checklistItemRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public GetChecklistService(
            ChecklistItemRepositoryPort checklistItemRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistItemResult> getChecklist(UUID userId, UUID workspaceId) {
        NoteAccess.requireOwnedWorkspace(userId, workspaceId, workspaceRepository);
        return checklistItemRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(item -> new ChecklistItemResult(
                        item.getId(), item.getDate(), item.getText(), item.isDone(), item.getPosition()))
                .toList();
    }
}
