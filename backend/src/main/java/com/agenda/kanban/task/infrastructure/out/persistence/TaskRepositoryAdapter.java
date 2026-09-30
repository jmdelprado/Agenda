package com.agenda.kanban.task.infrastructure.out.persistence;

import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final SpringDataTaskJpaRepository jpaRepository;

    public TaskRepositoryAdapter(SpringDataTaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Task save(Task task) {
        Instant createdAt = jpaRepository.findById(task.getId())
                .map(TaskJpaEntity::getCreatedAt)
                .orElseGet(Instant::now);
        TaskJpaEntity saved = jpaRepository.save(TaskMapper.toJpaEntity(task, createdAt));
        return TaskMapper.toDomain(saved);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return jpaRepository.findById(id).map(TaskMapper::toDomain);
    }

    @Override
    public List<Task> findByColumnIdOrderByCreatedAt(UUID columnId) {
        return jpaRepository.findByColumnIdOrderByCreatedAtAsc(columnId).stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
