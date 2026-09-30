package com.agenda.kanban.calendar.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: lista los eventos de Google Calendar del usuario para un día concreto.
 * Si el usuario no tiene conexión, o esta deja de ser válida, devuelve una lista vacía en vez de fallar.
 */
public interface GetGoogleCalendarEventsUseCase {

    List<CalendarEventResult> getEvents(UUID userId, LocalDate date);

    /** {@code accepted}: respuesta del usuario autenticado a la invitación (true si no aplica, p.ej. evento propio sin invitados). */
    record CalendarEventResult(
            String id, String title, Instant start, Instant end, boolean allDay, String location, String htmlLink,
            boolean accepted) {
    }
}
