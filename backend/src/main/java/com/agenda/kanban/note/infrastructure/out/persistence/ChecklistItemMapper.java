package com.agenda.kanban.note.infrastructure.out.persistence;

import com.agenda.kanban.note.domain.model.ChecklistItem;

final class ChecklistItemMapper {

    private ChecklistItemMapper() {
    }

    static ChecklistItemJpaEntity toJpaEntity(ChecklistItem item) {
        return new ChecklistItemJpaEntity(
                item.getId(), item.getWorkspaceId(), item.getDate(), item.getText(), item.isDone(),
                item.getPosition(), item.getCreatedAt());
    }

    static ChecklistItem toDomain(ChecklistItemJpaEntity entity) {
        return new ChecklistItem(
                entity.getId(), entity.getWorkspaceId(), entity.getItemDate(), entity.getText(), entity.isDone(),
                entity.getPosition(), entity.getCreatedAt());
    }
}
