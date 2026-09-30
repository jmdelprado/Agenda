package com.agenda.kanban.board.infrastructure.out.persistence;

import com.agenda.kanban.board.domain.model.Column;

final class ColumnMapper {

    private ColumnMapper() {
    }

    static ColumnJpaEntity toJpaEntity(Column column) {
        return new ColumnJpaEntity(column.getId(), column.getBoardId(), column.getName(), column.getPosition());
    }

    static Column toDomain(ColumnJpaEntity entity) {
        return new Column(entity.getId(), entity.getBoardId(), entity.getName(), entity.getPosition());
    }
}
