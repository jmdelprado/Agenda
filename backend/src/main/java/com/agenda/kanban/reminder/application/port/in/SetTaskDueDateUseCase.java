package com.agenda.kanban.reminder.application.port.in;

import java.time.Instant;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "asignar/modificar/eliminar la fecha límite de una tarea"
 * (PATCH /tasks/{taskId} — campos dueAt/reminderLeadMinutes, US2-AS1/AS4). Recalcula o cancela
 * el {@code Reminder} asociado según corresponda.
 */
public interface SetTaskDueDateUseCase {

    TaskDueDateResult setDueDate(SetTaskDueDateCommand command);

    /** {@code dueAt = null} elimina la fecha límite (y cancela el recordatorio pendiente). */
    record SetTaskDueDateCommand(UUID userId, UUID taskId, Instant dueAt, Integer reminderLeadMinutes) {
    }

    record TaskDueDateResult(UUID taskId, Instant dueAt, Integer reminderLeadMinutes, boolean overdue) {
    }
}
