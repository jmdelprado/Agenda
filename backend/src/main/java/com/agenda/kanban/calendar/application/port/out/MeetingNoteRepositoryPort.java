package com.agenda.kanban.calendar.application.port.out;

import com.agenda.kanban.calendar.domain.model.MeetingNote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link MeetingNote}. */
public interface MeetingNoteRepositoryPort {

    Optional<MeetingNote> findByUserIdAndEventId(UUID userId, String eventId);

    List<MeetingNote> findAllByUserId(UUID userId);

    MeetingNote save(MeetingNote note);
}
