package com.agenda.kanban.reminder.application.service;

import com.agenda.kanban.reminder.application.port.in.CancelReminderUseCase;
import com.agenda.kanban.reminder.application.port.out.ReminderRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cancela el recordatorio PENDING de una tarea, si existe. Ver {@link CancelReminderUseCase}. */
@Service
public class CancelReminderService implements CancelReminderUseCase {

    private final ReminderRepositoryPort reminderRepository;

    public CancelReminderService(ReminderRepositoryPort reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    @Transactional
    public void cancelForTask(UUID taskId) {
        reminderRepository.findPendingByTaskId(taskId)
                .ifPresent(reminder -> reminderRepository.save(reminder.cancel()));
    }
}
