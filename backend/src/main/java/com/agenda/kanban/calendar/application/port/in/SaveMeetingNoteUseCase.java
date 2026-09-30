package com.agenda.kanban.calendar.application.port.in;

import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase.MeetingNoteResult;
import java.util.UUID;

/** Puerto de entrada: escribir/actualizar la nota de una reunión (upsert por eventId). */
public interface SaveMeetingNoteUseCase {

    MeetingNoteResult save(SaveMeetingNoteCommand command);

    record SaveMeetingNoteCommand(UUID userId, String eventId, String content) {
    }
}
