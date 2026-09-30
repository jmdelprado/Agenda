package com.agenda.kanban.notification.application.service;

import com.agenda.kanban.notification.application.port.in.ListNotificationsUseCase;
import com.agenda.kanban.notification.application.port.out.NotificationRepositoryPort;
import com.agenda.kanban.notification.domain.model.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link ListNotificationsUseCase}. */
@Service
public class ListNotificationsService implements ListNotificationsUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public ListNotificationsService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<NotificationResult> listNotifications(ListNotificationsQuery query) {
        return notificationRepository.findByUserId(query.userId(), query.unreadOnly()).stream()
                .map(ListNotificationsService::toResult)
                .toList();
    }

    private static NotificationResult toResult(Notification notification) {
        return new NotificationResult(
                notification.getId(), notification.getMessage(), notification.getReminderId(),
                notification.getReadAt(), notification.getCreatedAt());
    }
}
