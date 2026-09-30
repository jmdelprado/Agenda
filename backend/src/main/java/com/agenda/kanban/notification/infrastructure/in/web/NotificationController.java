package com.agenda.kanban.notification.infrastructure.in.web;

import com.agenda.kanban.notification.application.port.in.ListNotificationsUseCase;
import com.agenda.kanban.notification.application.port.in.ListNotificationsUseCase.ListNotificationsQuery;
import com.agenda.kanban.notification.application.port.in.ListNotificationsUseCase.NotificationResult;
import com.agenda.kanban.notification.application.port.in.MarkNotificationReadUseCase;
import com.agenda.kanban.notification.application.port.in.MarkNotificationReadUseCase.MarkNotificationReadCommand;
import com.agenda.kanban.notification.application.port.in.MarkNotificationReadUseCase.NotificationReadResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para las notificaciones in-app (FR-014). El frontend las consulta
 * mediante short polling (research.md §4), no hay push en tiempo real en esta versión.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final ListNotificationsUseCase listNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    public NotificationController(
            ListNotificationsUseCase listNotificationsUseCase,
            MarkNotificationReadUseCase markNotificationReadUseCase) {
        this.listNotificationsUseCase = listNotificationsUseCase;
        this.markNotificationReadUseCase = markNotificationReadUseCase;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly) {
        List<NotificationResult> results =
                listNotificationsUseCase.listNotifications(new ListNotificationsQuery(userId, unreadOnly));
        return ResponseEntity.ok(results.stream().map(NotificationResponse::from).toList());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> markAsRead(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID notificationId) {
        NotificationReadResult result =
                markNotificationReadUseCase.markAsRead(new MarkNotificationReadCommand(userId, notificationId));
        return ResponseEntity.ok(new NotificationReadResponse(result.id(), result.readAt()));
    }

    public record NotificationResponse(
            UUID id, String message, UUID reminderId, Instant readAt, Instant createdAt) {
        static NotificationResponse from(NotificationResult result) {
            return new NotificationResponse(
                    result.id(), result.message(), result.reminderId(), result.readAt(), result.createdAt());
        }
    }

    public record NotificationReadResponse(UUID id, Instant readAt) {
    }
}
