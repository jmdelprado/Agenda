package com.agenda.kanban.note.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "eliminar un elemento de checklist". */
public interface DeleteChecklistItemUseCase {

    void delete(DeleteChecklistItemCommand command);

    record DeleteChecklistItemCommand(UUID userId, UUID itemId) {
    }
}
