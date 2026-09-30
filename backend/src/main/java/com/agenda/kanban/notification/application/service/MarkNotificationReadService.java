package com.agenda.kanban.notification.application.service;

import com.agenda.kanban.notification.application.port.in.MarkNotificationReadUseCase;
import com.agenda.kanban.notification.application.port.out.NotificationRepositoryPort;
import com.agenda.kanban.notification.domain.model.Notification;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link MarkNotificationReadUseCase}. */
@Service
public class MarkNotificationReadService implements MarkNotificationReadUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public MarkNotificationReadService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public NotificationReadResult markAsRead(MarkNotificationReadCommand command) {
        Notification notification = notificationRepository.findById(command.notificationId())
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (!notification.getUserId().equals(command.userId())) {
            throw new ForbiddenOperationException("La notificación no pertenece al usuario autenticado");
        }
        Notification saved = notificationRepository.save(notification.markRead(Instant.now()));
        return new NotificationReadResult(saved.getId(), saved.getReadAt());
    }
}
