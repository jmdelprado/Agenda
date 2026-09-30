package com.agenda.kanban.reminder.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.reminder.application.port.in.SetTaskDueDateUseCase;
import com.agenda.kanban.reminder.application.port.out.ReminderRepositoryPort;
import com.agenda.kanban.reminder.domain.model.Reminder;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asigna/modifica/elimina la fecha límite de una tarea y mantiene su {@link Reminder} coherente
 * (US2-AS1, US2-AS4): cancela el recordatorio PENDING anterior (si lo hay) y crea uno nuevo si la
 * tarea sigue teniendo dueAt tras la operación.
 *
 * <p>Vive en el módulo {@code reminder} (no en {@code task}) porque orquesta ambos agregados; para
 * la verificación de propiedad reutiliza los mismos puertos de salida que {@code TaskAccess} del
 * módulo task (no se puede reutilizar esa clase directamente: es package-private a su módulo).
 */
@Service
public class SetTaskDueDateService implements SetTaskDueDateUseCase {

    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final ReminderRepositoryPort reminderRepository;

    public SetTaskDueDateService(
            TaskRepositoryPort taskRepository,
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository,
            ReminderRepositoryPort reminderRepository) {
        this.taskRepository = taskRepository;
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
        this.reminderRepository = reminderRepository;
    }

    @Override
    @Transactional
    public TaskDueDateResult setDueDate(SetTaskDueDateCommand command) {
        Task task = requireOwnedTask(command.userId(), command.taskId());
        Task updated = task.withDueDate(command.dueAt(), command.reminderLeadMinutes());
        taskRepository.save(updated);

        reminderRepository.findPendingByTaskId(updated.getId())
                .ifPresent(pending -> reminderRepository.save(pending.cancel()));

        Instant triggerAt = updated.computeReminderTriggerAt();
        if (triggerAt != null) {
            reminderRepository.save(Reminder.schedule(UUID.randomUUID(), updated.getId(), triggerAt));
        }

        return new TaskDueDateResult(
                updated.getId(), updated.getDueAt(), updated.getReminderLeadMinutes(),
                updated.isOverdue(Instant.now()));
    }

    private Task requireOwnedTask(UUID userId, UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        Column column = columnRepository.findById(task.getColumnId())
                .orElseThrow(() -> new NotFoundException("Columna no encontrada"));
        Board board = boardRepository.findById(column.getBoardId())
                .orElseThrow(() -> new NotFoundException("Tablero no encontrado"));
        Workspace workspace = workspaceRepository.findById(board.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(userId)) {
            throw new ForbiddenOperationException("La tarea no pertenece al usuario autenticado");
        }
        return task;
    }
}
