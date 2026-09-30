package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase.MeetingNoteResult;
import com.agenda.kanban.calendar.application.port.in.SaveMeetingNoteUseCase;
import com.agenda.kanban.calendar.application.port.out.MeetingNoteRepositoryPort;
import com.agenda.kanban.calendar.domain.model.MeetingNote;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link SaveMeetingNoteUseCase}. Upsert: crea la nota si no existía, o actualiza su contenido. */
@Service
public class SaveMeetingNoteService implements SaveMeetingNoteUseCase {

    private final MeetingNoteRepositoryPort repository;

    public SaveMeetingNoteService(MeetingNoteRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public MeetingNoteResult save(SaveMeetingNoteCommand command) {
        Instant now = Instant.now();
        MeetingNote existing = repository.findByUserIdAndEventId(command.userId(), command.eventId()).orElse(null);
        MeetingNote toSave = existing != null
                ? existing.withContent(command.content(), now)
                : MeetingNote.create(command.userId(), command.eventId(), command.content(), now);
        MeetingNote saved = repository.save(toSave);
        return new MeetingNoteResult(saved.getEventId(), saved.getContent());
    }
}
