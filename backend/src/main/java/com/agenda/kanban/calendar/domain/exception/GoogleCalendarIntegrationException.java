package com.agenda.kanban.calendar.domain.exception;

/** Fallo de comunicación con los endpoints OAuth/Calendar de Google (token, userinfo o events). */
public class GoogleCalendarIntegrationException extends RuntimeException {

    public GoogleCalendarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
