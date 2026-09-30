package com.agenda.kanban.reminder.application.port.out;

import com.agenda.kanban.reminder.domain.model.Reminder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Reminder}. Implementado en infrastructure/out/persistence. */
public interface ReminderRepositoryPort {

    Reminder save(Reminder reminder);

    Optional<Reminder> findById(UUID id);

    /** El recordatorio PENDING más reciente de una tarea, si existe (normalmente hay como mucho uno). */
    Optional<Reminder> findPendingByTaskId(UUID taskId);

    /** Recordatorios PENDING cuyo triggerAt ya se alcanzó (candidatos a disparo, SC-003). */
    List<Reminder> findDueForDispatch(Instant now);
}
