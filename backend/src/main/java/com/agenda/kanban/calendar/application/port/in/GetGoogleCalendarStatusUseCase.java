package com.agenda.kanban.calendar.application.port.in;

import java.util.UUID;

/** Puerto de entrada: consulta si el usuario tiene Google Calendar conectado. */
public interface GetGoogleCalendarStatusUseCase {

    GoogleCalendarStatusResult getStatus(UUID userId);

    record GoogleCalendarStatusResult(boolean connected, String email) {
    }
}
