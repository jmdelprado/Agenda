package com.agenda.kanban.reminder.application.port.in;

/**
 * Puerto de entrada: caso de uso "procesar los recordatorios pendientes vencidos" (SC-003:
 * 0% de recordatorios perdidos). Invocado por el adaptador de scheduling
 * ({@code ReminderSchedulerAdapter}, infrastructure/out/scheduling) de forma periódica.
 * Procesamiento idempotente: cada recordatorio PENDING con triggerAt alcanzado se procesa como
 * mucho una vez por invocación (cambia de estado antes de terminar).
 */
public interface DispatchDueRemindersUseCase {

    DispatchResult dispatchDueReminders();

    record DispatchResult(int sent, int failed) {
    }
}
