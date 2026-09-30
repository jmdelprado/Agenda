package com.agenda.kanban.workspace.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorkspaceJpaRepository extends JpaRepository<WorkspaceJpaEntity, UUID> {

    List<WorkspaceJpaEntity> findAllByUserId(UUID userId);

    boolean existsByUserIdAndNameAndArchivedAtIsNull(UUID userId, String name);
}
