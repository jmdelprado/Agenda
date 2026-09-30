package com.agenda.kanban.reminder.application.service;

import com.agenda.kanban.auth.application.port.out.UserRepositoryPort;
import com.agenda.kanban.auth.domain.model.User;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.notification.application.port.out.EmailSenderPort;
import com.agenda.kanban.notification.application.port.out.NotificationRepositoryPort;
import com.agenda.kanban.notification.domain.model.Notification;
import com.agenda.kanban.reminder.application.port.in.DispatchDueRemindersUseCase;
import com.agenda.kanban.reminder.application.port.out.ReminderRepositoryPort;
import com.agenda.kanban.reminder.domain.model.Reminder;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Procesa de forma idempotente los recordatorios PENDING cuyo triggerAt ya se alcanzó (SC-003):
 * crea la Notification in-app, intenta enviar el email y marca el recordatorio SENT/FAILED.
 * Invocado periódicamente por {@code ReminderSchedulerAdapter} (infrastructure/out/scheduling).
 */
@Service
public class DispatchDueRemindersService implements DispatchDueRemindersUseCase {

    private static final Logger log = LoggerFactory.getLogger(DispatchDueRemindersService.class);
    private static final DateTimeFormatter DUE_AT_FORMAT =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(new Locale("es", "ES"));

    private final ReminderRepositoryPort reminderRepository;
    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final EmailSenderPort emailSender;

    public DispatchDueRemindersService(
            ReminderRepositoryPort reminderRepository,
            TaskRepositoryPort taskRepository,
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository,
            UserRepositoryPort userRepository,
            NotificationRepositoryPort notificationRepository,
            EmailSenderPort emailSender) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public DispatchResult dispatchDueReminders() {
        Instant now = Instant.now();
        List<Reminder> due = reminderRepository.findDueForDispatch(now);
        int sent = 0;
        int failed = 0;
        for (Reminder reminder : due) {
            try {
                if (dispatchOne(reminder, now)) {
                    sent++;
                }
            } catch (Exception ex) {
                log.warn("No se pudo procesar el recordatorio {}: {}", reminder.getId(), ex.getMessage());
                reminderRepository.save(reminder.markFailed());
                failed++;
            }
        }
        return new DispatchResult(sent, failed);
    }

    /** @return true si se marcó SENT (false si la tarea ya no aplica y simplemente se canceló). */
    private boolean dispatchOne(Reminder reminder, Instant now) {
        Task task = taskRepository.findById(reminder.getTaskId()).orElse(null);
        if (task == null || task.isCompleted() || task.getDueAt() == null) {
            // La tarea se eliminó, se completó o perdió su fecha límite entre el cálculo y el disparo.
            reminderRepository.save(reminder.cancel());
            return false;
        }

        Column column = columnRepository.findById(task.getColumnId())
                .orElseThrow(() -> new IllegalStateException("Columna no encontrada para la tarea " + task.getId()));
        Board board = boardRepository.findById(column.getBoardId())
                .orElseThrow(() -> new IllegalStateException("Tablero no encontrado para la columna " + column.getId()));
        Workspace workspace = workspaceRepository.findById(board.getWorkspaceId())
                .orElseThrow(() -> new IllegalStateException("Workspace no encontrado para el tablero " + board.getId()));
        User user = userRepository.findById(workspace.getUserId())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para el workspace " + workspace.getId()));

        String message = "\"" + task.getTitle() + "\" vence el " + DUE_AT_FORMAT.format(
                task.getDueAt().atZone(java.time.ZoneId.systemDefault()));
        Notification notification =
                Notification.create(UUID.randomUUID(), user.getId(), reminder.getId(), message, now);
        notificationRepository.save(notification);

        try {
            emailSender.send(user.getEmail(), "Recordatorio: " + task.getTitle(), message);
        } catch (Exception mailEx) {
            // El canal in-app ya quedó registrado; no se re-lanza para no marcar el recordatorio como FAILED
            // solo por un fallo de email (research.md §3: 0 recordatorios perdidos vía al menos un canal).
            log.warn("Fallo enviando el email del recordatorio {}: {}", reminder.getId(), mailEx.getMessage());
        }

        reminderRepository.save(reminder.markSent(now));
        return true;
    }
}
