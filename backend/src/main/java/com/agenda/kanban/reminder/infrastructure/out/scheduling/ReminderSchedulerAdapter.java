package com.agenda.kanban.reminder.infrastructure.out.scheduling;

import com.agenda.kanban.reminder.application.port.in.DispatchDueRemindersUseCase;
import com.agenda.kanban.reminder.application.port.in.DispatchDueRemindersUseCase.DispatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: dispara periódicamente el procesamiento de recordatorios vencidos
 * (research.md §3, SC-003: 0% de recordatorios perdidos). El recordatorio se persiste en base de
 * datos (no en memoria), así que este job sobrevive a reinicios del backend: en cada arranque
 * retoma cualquier recordatorio PENDING cuyo triggerAt ya haya pasado.
 */
@Component
public class ReminderSchedulerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerAdapter.class);

    private final DispatchDueRemindersUseCase dispatchDueRemindersUseCase;

    public ReminderSchedulerAdapter(DispatchDueRemindersUseCase dispatchDueRemindersUseCase) {
        this.dispatchDueRemindersUseCase = dispatchDueRemindersUseCase;
    }

    // T074: usa app.reminder.dispatch-fixed-delay-ms (application.yml) en vez de un valor fijo en
    // código — la propiedad ya existía configurada pero no estaba conectada a este @Scheduled.
    @Scheduled(fixedDelayString = "${app.reminder.dispatch-fixed-delay-ms}")
    public void dispatchDueReminders() {
        DispatchResult result = dispatchDueRemindersUseCase.dispatchDueReminders();
        if (result.sent() > 0 || result.failed() > 0) {
            log.info("Recordatorios procesados: {} enviados, {} fallidos", result.sent(), result.failed());
        }
    }
}
