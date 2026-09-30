package com.agenda.kanban.note.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: caso de uso "consultar toda la checklist de un espacio de trabajo". */
public interface GetChecklistUseCase {

    List<ChecklistItemResult> getChecklist(UUID userId, UUID workspaceId);

    record ChecklistItemResult(UUID id, LocalDate date, String text, boolean done, int position) {
    }
}
