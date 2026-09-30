package com.agenda.kanban.notification.infrastructure.out.persistence;

import com.agenda.kanban.notification.domain.model.Notification;

final class NotificationMapper {

    private NotificationMapper() {
    }

    static NotificationJpaEntity toJpaEntity(Notification notification) {
        return new NotificationJpaEntity(
                notification.getId(), notification.getUserId(), notification.getReminderId(),
                notification.getMessage(), notification.getReadAt(), notification.getCreatedAt());
    }

    static Notification toDomain(NotificationJpaEntity entity) {
        return new Notification(
                entity.getId(), entity.getUserId(), entity.getReminderId(), entity.getMessage(),
                entity.getReadAt(), entity.getCreatedAt());
    }
}
