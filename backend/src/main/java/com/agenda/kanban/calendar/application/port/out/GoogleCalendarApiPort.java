package com.agenda.kanban.calendar.application.port.out;

import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarEventsUseCase.CalendarEventResult;
import java.time.Instant;
import java.util.List;

/** Puerto de salida: lista los eventos de la Google Calendar API para un rango [timeMin, timeMax). */
public interface GoogleCalendarApiPort {

    List<CalendarEventResult> listEvents(String accessToken, Instant timeMin, Instant timeMax);
}
