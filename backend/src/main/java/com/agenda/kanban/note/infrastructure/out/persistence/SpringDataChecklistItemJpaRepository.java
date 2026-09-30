package com.agenda.kanban.note.infrastructure.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChecklistItemJpaRepository extends JpaRepository<ChecklistItemJpaEntity, UUID> {

    List<ChecklistItemJpaEntity> findAllByWorkspaceIdOrderByItemDateAscPositionAsc(UUID workspaceId);

    int countByWorkspaceIdAndItemDate(UUID workspaceId, LocalDate itemDate);
}
