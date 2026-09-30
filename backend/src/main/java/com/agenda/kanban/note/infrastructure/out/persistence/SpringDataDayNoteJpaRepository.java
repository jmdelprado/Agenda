package com.agenda.kanban.note.infrastructure.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDayNoteJpaRepository extends JpaRepository<DayNoteJpaEntity, UUID> {

    Optional<DayNoteJpaEntity> findByWorkspaceIdAndNoteDate(UUID workspaceId, LocalDate noteDate);

    List<DayNoteJpaEntity> findAllByWorkspaceIdOrderByNoteDateAsc(UUID workspaceId);
}
