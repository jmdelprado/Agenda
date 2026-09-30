package com.agenda.kanban.board.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataColumnJpaRepository extends JpaRepository<ColumnJpaEntity, UUID> {

    List<ColumnJpaEntity> findByBoardIdOrderByPositionAsc(UUID boardId);

    int countByBoardId(UUID boardId);
}
