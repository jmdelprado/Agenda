package com.agenda.kanban.workspace.infrastructure.out.persistence;

import com.agenda.kanban.workspace.domain.model.Workspace;

final class WorkspaceMapper {

    private WorkspaceMapper() {
    }

    static WorkspaceJpaEntity toJpaEntity(Workspace workspace) {
        return new WorkspaceJpaEntity(
                workspace.getId(),
                workspace.getUserId(),
                workspace.getName(),
                workspace.getCreatedAt(),
                workspace.getArchivedAt());
    }

    static Workspace toDomain(WorkspaceJpaEntity entity) {
        return new Workspace(
                entity.getId(), entity.getUserId(), entity.getName(), entity.getCreatedAt(), entity.getArchivedAt());
    }
}
