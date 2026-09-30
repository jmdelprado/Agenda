package com.agenda.kanban.notification.infrastructure.out.persistence;

import com.agenda.kanban.notification.application.port.out.NotificationRepositoryPort;
import com.agenda.kanban.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return NotificationMapper.toDomain(jpaRepository.save(NotificationMapper.toJpaEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(UUID userId, boolean unreadOnly) {
        var entities = unreadOnly
                ? jpaRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId)
                : jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return entities.stream().map(NotificationMapper::toDomain).toList();
    }
}
