package com.agenda.kanban.reminder.infrastructure.out.persistence;

import com.agenda.kanban.reminder.application.port.out.ReminderRepositoryPort;
import com.agenda.kanban.reminder.domain.model.Reminder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReminderRepositoryAdapter implements ReminderRepositoryPort {

    private final SpringDataReminderJpaRepository jpaRepository;

    public ReminderRepositoryAdapter(SpringDataReminderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Reminder save(Reminder reminder) {
        return ReminderMapper.toDomain(jpaRepository.save(ReminderMapper.toJpaEntity(reminder)));
    }

    @Override
    public Optional<Reminder> findById(UUID id) {
        return jpaRepository.findById(id).map(ReminderMapper::toDomain);
    }

    @Override
    public Optional<Reminder> findPendingByTaskId(UUID taskId) {
        return jpaRepository.findPendingByTaskId(taskId).map(ReminderMapper::toDomain);
    }

    @Override
    public List<Reminder> findDueForDispatch(Instant now) {
        return jpaRepository.findDueForDispatch(now).stream().map(ReminderMapper::toDomain).toList();
    }
}
