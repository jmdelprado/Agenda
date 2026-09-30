package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase;
import com.agenda.kanban.calendar.application.port.out.MeetingNoteRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link GetMeetingNotesUseCase}. */
@Service
public class GetMeetingNotesService implements GetMeetingNotesUseCase {

    private final MeetingNoteRepositoryPort repository;

    public GetMeetingNotesService(MeetingNoteRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingNoteResult> getNotes(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(note -> new MeetingNoteResult(note.getEventId(), note.getContent()))
                .toList();
    }
}
