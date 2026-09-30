package com.agenda.kanban.note.application.port.in;

import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase.DayNoteResult;
import java.time.LocalDate;
import java.util.UUID;

/** Puerto de entrada: caso de uso "escribir/actualizar la nota de un día" (upsert por fecha). */
public interface SaveDayNoteUseCase {

    DayNoteResult save(SaveDayNoteCommand command);

    record SaveDayNoteCommand(UUID userId, UUID workspaceId, LocalDate date, String content) {
    }
}
