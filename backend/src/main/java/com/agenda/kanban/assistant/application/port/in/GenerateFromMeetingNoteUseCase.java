package com.agenda.kanban.assistant.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de entrada: caso de uso "generar con IA" a partir de la nota de una reunión — lee la
 * nota ya guardada (ver {@link com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase})
 * y usa un modelo de IA para proponer tareas de tablero, elementos de checklist y un resumen para la nota del día.
 */
public interface GenerateFromMeetingNoteUseCase {

    GenerateResult generate(GenerateCommand command);

    record GenerateCommand(UUID userId, UUID workspaceId, LocalDate date, String eventId, String meetingTitle) {
    }

    record GenerateResult(int tasksCreated, int checklistItemsCreated, boolean noteUpdated) {
    }
}
