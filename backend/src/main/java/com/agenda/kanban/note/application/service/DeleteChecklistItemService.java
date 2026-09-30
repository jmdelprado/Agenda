package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.DeleteChecklistItemUseCase;
import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.note.domain.model.ChecklistItem;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link DeleteChecklistItemUseCase}. */
@Service
public class DeleteChecklistItemService implements DeleteChecklistItemUseCase {

    private final ChecklistItemRepositoryPort checklistItemRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public DeleteChecklistItemService(
            ChecklistItemRepositoryPort checklistItemRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteChecklistItemCommand command) {
        ChecklistItem item = NoteAccess.requireOwnedItem(
                command.userId(), command.itemId(), checklistItemRepository, workspaceRepository);
        checklistItemRepository.deleteById(item.getId());
    }
}
