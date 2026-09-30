package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.CreateChecklistItemUseCase;
import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.note.domain.model.ChecklistItem;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link CreateChecklistItemUseCase}. La posición se calcula al final de las ya existentes ese día. */
@Service
public class CreateChecklistItemService implements CreateChecklistItemUseCase {

    private final ChecklistItemRepositoryPort checklistItemRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public CreateChecklistItemService(
            ChecklistItemRepositoryPort checklistItemRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public ChecklistItemResult create(CreateChecklistItemCommand command) {
        NoteAccess.requireOwnedWorkspace(command.userId(), command.workspaceId(), workspaceRepository);
        int position = checklistItemRepository.countByWorkspaceIdAndDate(command.workspaceId(), command.date());
        ChecklistItem saved = checklistItemRepository.save(ChecklistItem.create(
                UUID.randomUUID(), command.workspaceId(), command.date(), command.text(), position, Instant.now()));
        return new ChecklistItemResult(
                saved.getId(), saved.getDate(), saved.getText(), saved.isDone(), saved.getPosition());
    }
}
