package com.agenda.kanban.note.application.port.in;

import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import java.time.LocalDate;
import java.util.UUID;

/** Puerto de entrada: caso de uso "apuntar un elemento de checklist en un día". */
public interface CreateChecklistItemUseCase {

    ChecklistItemResult create(CreateChecklistItemCommand command);

    record CreateChecklistItemCommand(UUID userId, UUID workspaceId, LocalDate date, String text) {
    }
}
