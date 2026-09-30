package com.agenda.kanban.task.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaskJpaRepository extends JpaRepository<TaskJpaEntity, UUID> {

    List<TaskJpaEntity> findByColumnIdOrderByCreatedAtAsc(UUID columnId);
}
