package com.agenda.kanban.note.infrastructure.out.persistence;

import com.agenda.kanban.note.application.port.out.ChecklistItemRepositoryPort;
import com.agenda.kanban.note.domain.model.ChecklistItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ChecklistItemRepositoryAdapter implements ChecklistItemRepositoryPort {

    private final SpringDataChecklistItemJpaRepository jpaRepository;

    public ChecklistItemRepositoryAdapter(SpringDataChecklistItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ChecklistItem save(ChecklistItem item) {
        ChecklistItemJpaEntity saved = jpaRepository.save(ChecklistItemMapper.toJpaEntity(item));
        return ChecklistItemMapper.toDomain(saved);
    }

    @Override
    public Optional<ChecklistItem> findById(UUID id) {
        return jpaRepository.findById(id).map(ChecklistItemMapper::toDomain);
    }

    @Override
    public List<ChecklistItem> findAllByWorkspaceId(UUID workspaceId) {
        return jpaRepository.findAllByWorkspaceIdOrderByItemDateAscPositionAsc(workspaceId).stream()
                .map(ChecklistItemMapper::toDomain)
                .toList();
    }

    @Override
    public int countByWorkspaceIdAndDate(UUID workspaceId, LocalDate date) {
        return jpaRepository.countByWorkspaceIdAndItemDate(workspaceId, date);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
