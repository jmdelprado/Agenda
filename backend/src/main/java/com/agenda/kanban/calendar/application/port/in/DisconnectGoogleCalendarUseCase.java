package com.agenda.kanban.calendar.application.port.in;

import java.util.UUID;

/** Puerto de entrada: olvida la conexión de Google Calendar del usuario (no revoca el token en Google). */
public interface DisconnectGoogleCalendarUseCase {

    void disconnect(UUID userId);
}
