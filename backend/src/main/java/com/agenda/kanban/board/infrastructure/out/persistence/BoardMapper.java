package com.agenda.kanban.board.infrastructure.out.persistence;

import com.agenda.kanban.board.domain.model.Board;

final class BoardMapper {

    private BoardMapper() {
    }

    static BoardJpaEntity toJpaEntity(Board board) {
        return new BoardJpaEntity(board.getId(), board.getWorkspaceId());
    }

    static Board toDomain(BoardJpaEntity entity) {
        return new Board(entity.getId(), entity.getWorkspaceId());
    }
}
