package com.agenda.kanban.task.infrastructure.out.persistence;

import com.agenda.kanban.task.domain.model.Task;
import java.time.Instant;

final class TaskMapper {

    private TaskMapper() {
    }

    static TaskJpaEntity toJpaEntity(Task task, Instant createdAt) {
        return new TaskJpaEntity(
                task.getId(), task.getColumnId(), task.getTitle(), task.getDescription(), task.getDueAt(),
                task.getReminderLeadMinutes(), task.getCompletedAt(), createdAt);
    }

    static Task toDomain(TaskJpaEntity entity) {
        return new Task(
                entity.getId(), entity.getColumnId(), entity.getTitle(), entity.getDescription(),
                entity.getDueAt(), entity.getReminderLeadMinutes(), entity.getCompletedAt());
    }
}
