package com.agenda.kanban.board.infrastructure.out.persistence;

import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Column;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ColumnRepositoryAdapter implements ColumnRepositoryPort {

    private final SpringDataColumnJpaRepository jpaRepository;

    public ColumnRepositoryAdapter(SpringDataColumnJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Column save(Column column) {
        ColumnJpaEntity saved = jpaRepository.save(ColumnMapper.toJpaEntity(column));
        return ColumnMapper.toDomain(saved);
    }

    @Override
    public Optional<Column> findById(UUID id) {
        return jpaRepository.findById(id).map(ColumnMapper::toDomain);
    }

    @Override
    public List<Column> findByBoardIdOrderByPosition(UUID boardId) {
        return jpaRepository.findByBoardIdOrderByPositionAsc(boardId).stream().map(ColumnMapper::toDomain).toList();
    }

    @Override
    public int countByBoardId(UUID boardId) {
        return jpaRepository.countByBoardId(boardId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
