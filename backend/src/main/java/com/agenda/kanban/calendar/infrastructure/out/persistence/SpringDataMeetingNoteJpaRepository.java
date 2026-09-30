package com.agenda.kanban.calendar.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMeetingNoteJpaRepository extends JpaRepository<MeetingNoteJpaEntity, UUID> {

    Optional<MeetingNoteJpaEntity> findByUserIdAndEventId(UUID userId, String eventId);

    List<MeetingNoteJpaEntity> findAllByUserId(UUID userId);
}
