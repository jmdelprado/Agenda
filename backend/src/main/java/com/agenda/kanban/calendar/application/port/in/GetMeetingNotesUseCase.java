package com.agenda.kanban.calendar.application.port.in;

import java.util.List;
import java.util.UUID;

/** Puerto de entrada: consulta todas las notas de reunión del usuario. */
public interface GetMeetingNotesUseCase {

    List<MeetingNoteResult> getNotes(UUID userId);

    record MeetingNoteResult(String eventId, String content) {
    }
}
