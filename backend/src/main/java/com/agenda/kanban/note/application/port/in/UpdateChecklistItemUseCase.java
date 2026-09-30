package com.agenda.kanban.note.application.port.in;

import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import java.util.UUID;

/** Puerto de entrada: caso de uso "marcar hecho / editar texto de un elemento de checklist". */
public interface UpdateChecklistItemUseCase {

    ChecklistItemResult update(UpdateChecklistItemCommand command);

    record UpdateChecklistItemCommand(UUID userId, UUID itemId, Boolean done, String text) {
    }
}
