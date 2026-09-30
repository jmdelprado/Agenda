package com.agenda.kanban.notification.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataNotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<NotificationJpaEntity> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);
}
