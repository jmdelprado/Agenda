package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import com.agenda.kanban.note.application.port.in.UpdateChecklistItemUseCase;
import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.note.domain.model.ChecklistItem;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link UpdateChecklistItemUseCase}. Actualización parcial: solo se tocan los campos presentes. */
@Service
public class UpdateChecklistItemService implements UpdateChecklistItemUseCase {

    private final ChecklistItemRepositoryPort checklistItemRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public UpdateChecklistItemService(
            ChecklistItemRepositoryPort checklistItemRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public ChecklistItemResult update(UpdateChecklistItemCommand command) {
        ChecklistItem item = NoteAccess.requireOwnedItem(
                command.userId(), command.itemId(), checklistItemRepository, workspaceRepository);
        ChecklistItem updated = item;
        if (command.text() != null) {
            updated = updated.withText(command.text());
        }
        if (command.done() != null) {
            updated = updated.withDone(command.done());
        }
        ChecklistItem saved = checklistItemRepository.save(updated);
        return new ChecklistItemResult(
                saved.getId(), saved.getDate(), saved.getText(), saved.isDone(), saved.getPosition());
    }
}
