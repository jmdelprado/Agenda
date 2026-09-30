package com.agenda.kanban.workspace.infrastructure.out.persistence;

import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceRepositoryAdapter implements WorkspaceRepositoryPort {

    private final SpringDataWorkspaceJpaRepository jpaRepository;

    public WorkspaceRepositoryAdapter(SpringDataWorkspaceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Workspace save(Workspace workspace) {
        WorkspaceJpaEntity saved = jpaRepository.save(WorkspaceMapper.toJpaEntity(workspace));
        return WorkspaceMapper.toDomain(saved);
    }

    @Override
    public Optional<Workspace> findById(UUID id) {
        return jpaRepository.findById(id).map(WorkspaceMapper::toDomain);
    }

    @Override
    public List<Workspace> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(WorkspaceMapper::toDomain).toList();
    }

    @Override
    public boolean existsActiveByUserIdAndName(UUID userId, String name) {
        return jpaRepository.existsByUserIdAndNameAndArchivedAtIsNull(userId, name);
    }
}
