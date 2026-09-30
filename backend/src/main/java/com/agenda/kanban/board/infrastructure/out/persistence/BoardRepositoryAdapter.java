package com.agenda.kanban.board.infrastructure.out.persistence;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BoardRepositoryAdapter implements BoardRepositoryPort {

    private final SpringDataBoardJpaRepository jpaRepository;

    public BoardRepositoryAdapter(SpringDataBoardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Board save(Board board) {
        BoardJpaEntity saved = jpaRepository.save(BoardMapper.toJpaEntity(board));
        return BoardMapper.toDomain(saved);
    }

    @Override
    public Optional<Board> findById(UUID id) {
        return jpaRepository.findById(id).map(BoardMapper::toDomain);
    }

    @Override
    public Optional<Board> findByWorkspaceId(UUID workspaceId) {
        return jpaRepository.findByWorkspaceId(workspaceId).map(BoardMapper::toDomain);
    }
}
