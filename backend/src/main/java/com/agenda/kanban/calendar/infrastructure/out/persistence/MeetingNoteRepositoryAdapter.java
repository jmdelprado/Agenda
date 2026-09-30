package com.agenda.kanban.calendar.infrastructure.out.persistence;

import com.agenda.kanban.calendar.application.port.out.MeetingNoteRepositoryPort;
import com.agenda.kanban.calendar.domain.model.MeetingNote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MeetingNoteRepositoryAdapter implements MeetingNoteRepositoryPort {

    private final SpringDataMeetingNoteJpaRepository jpaRepository;

    public MeetingNoteRepositoryAdapter(SpringDataMeetingNoteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<MeetingNote> findByUserIdAndEventId(UUID userId, String eventId) {
        return jpaRepository.findByUserIdAndEventId(userId, eventId).map(MeetingNoteMapper::toDomain);
    }

    @Override
    public List<MeetingNote> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(MeetingNoteMapper::toDomain).toList();
    }

    @Override
    public MeetingNote save(MeetingNote note) {
        MeetingNoteJpaEntity saved = jpaRepository.save(MeetingNoteMapper.toJpaEntity(note));
        return MeetingNoteMapper.toDomain(saved);
    }
}
