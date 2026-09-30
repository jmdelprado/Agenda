package com.agenda.kanban.reminder.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "cancelar el recordatorio pendiente de una tarea". Lo usa
 * {@link SetTaskDueDateUseCase} internamente y, en User Story 3, la eliminación de un espacio
 * de trabajo completo (cancela los recordatorios de todas sus tareas).
 */
public interface CancelReminderUseCase {

    /** No-op si la tarea no tiene ningún recordatorio PENDING. */
    void cancelForTask(UUID taskId);
}
