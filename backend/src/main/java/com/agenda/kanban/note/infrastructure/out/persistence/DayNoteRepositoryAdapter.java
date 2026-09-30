package com.agenda.kanban.note.infrastructure.out.persistence;

import com.agenda.kanban.note.application.port.out.DayNoteRepositoryPort;
import com.agenda.kanban.note.domain.model.DayNote;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DayNoteRepositoryAdapter implements DayNoteRepositoryPort {

    private final SpringDataDayNoteJpaRepository jpaRepository;

    public DayNoteRepositoryAdapter(SpringDataDayNoteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DayNote save(DayNote note) {
        DayNoteJpaEntity saved = jpaRepository.save(DayNoteMapper.toJpaEntity(note));
        return DayNoteMapper.toDomain(saved);
    }

    @Override
    public Optional<DayNote> findByWorkspaceIdAndDate(UUID workspaceId, LocalDate date) {
        return jpaRepository.findByWorkspaceIdAndNoteDate(workspaceId, date).map(DayNoteMapper::toDomain);
    }

    @Override
    public List<DayNote> findAllByWorkspaceId(UUID workspaceId) {
        return jpaRepository.findAllByWorkspaceIdOrderByNoteDateAsc(workspaceId).stream()
                .map(DayNoteMapper::toDomain)
                .toList();
    }
}
