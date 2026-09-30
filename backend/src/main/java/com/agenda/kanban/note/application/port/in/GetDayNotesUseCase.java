package com.agenda.kanban.note.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: caso de uso "consultar todas las notas de un espacio de trabajo". */
public interface GetDayNotesUseCase {

    List<DayNoteResult> getNotes(UUID userId, UUID workspaceId);

    record DayNoteResult(LocalDate date, String content) {
    }
}
