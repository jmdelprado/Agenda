package com.agenda.kanban.calendar.infrastructure.in.web;

import com.agenda.kanban.calendar.application.port.in.DisconnectGoogleCalendarUseCase;
import com.agenda.kanban.calendar.application.port.in.GetGoogleAuthUrlUseCase;
import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarEventsUseCase;
import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarEventsUseCase.CalendarEventResult;
import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarStatusUseCase;
import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarStatusUseCase.GoogleCalendarStatusResult;
import com.agenda.kanban.calendar.application.port.in.HandleGoogleCallbackUseCase;
import com.agenda.kanban.calendar.application.port.in.HandleGoogleCallbackUseCase.HandleGoogleCallbackCommand;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la conexión de Google Calendar: iniciar el flujo OAuth, recibir
 * el callback de Google (endpoint público, ver SecurityConfig), consultar el estado, desconectar
 * y listar los eventos de un día para el panel "Reuniones" de la Agenda.
 */
@RestController
@RequestMapping("/api/v1/calendar/google")
public class GoogleCalendarController {

    private final GetGoogleAuthUrlUseCase getGoogleAuthUrlUseCase;
    private final HandleGoogleCallbackUseCase handleGoogleCallbackUseCase;
    private final GetGoogleCalendarStatusUseCase getGoogleCalendarStatusUseCase;
    private final DisconnectGoogleCalendarUseCase disconnectGoogleCalendarUseCase;
    private final GetGoogleCalendarEventsUseCase getGoogleCalendarEventsUseCase;
    private final String frontendRedirectBase;

    public GoogleCalendarController(
            GetGoogleAuthUrlUseCase getGoogleAuthUrlUseCase,
            HandleGoogleCallbackUseCase handleGoogleCallbackUseCase,
            GetGoogleCalendarStatusUseCase getGoogleCalendarStatusUseCase,
            DisconnectGoogleCalendarUseCase disconnectGoogleCalendarUseCase,
            GetGoogleCalendarEventsUseCase getGoogleCalendarEventsUseCase,
            @Value("${app.google.frontend-redirect-base}") String frontendRedirectBase) {
        this.getGoogleAuthUrlUseCase = getGoogleAuthUrlUseCase;
        this.handleGoogleCallbackUseCase = handleGoogleCallbackUseCase;
        this.getGoogleCalendarStatusUseCase = getGoogleCalendarStatusUseCase;
        this.disconnectGoogleCalendarUseCase = disconnectGoogleCalendarUseCase;
        this.getGoogleCalendarEventsUseCase = getGoogleCalendarEventsUseCase;
        this.frontendRedirectBase = frontendRedirectBase;
    }

    @GetMapping("/connect-url")
    public ResponseEntity<ConnectUrlResponse> getConnectUrl(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(new ConnectUrlResponse(getGoogleAuthUrlUseCase.getAuthUrl(userId)));
    }

    /** Público (ver SecurityConfig): Google redirige aquí el navegador desnudo, sin cabecera Authorization. */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code, @RequestParam(required = false) String state) {
        try {
            if (code == null || state == null) {
                throw new IllegalArgumentException("Faltan parámetros code/state");
            }
            handleGoogleCallbackUseCase.handleCallback(new HandleGoogleCallbackCommand(code, state));
            return redirectTo(frontendRedirectBase + "/agenda?calendar=connected");
        } catch (RuntimeException ex) {
            return redirectTo(frontendRedirectBase + "/agenda?calendar=error");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<GoogleCalendarStatusResponse> getStatus(@AuthenticationPrincipal UUID userId) {
        GoogleCalendarStatusResult result = getGoogleCalendarStatusUseCase.getStatus(userId);
        return ResponseEntity.ok(new GoogleCalendarStatusResponse(result.connected(), result.email()));
    }

    @DeleteMapping
    public ResponseEntity<Void> disconnect(@AuthenticationPrincipal UUID userId) {
        disconnectGoogleCalendarUseCase.disconnect(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @AuthenticationPrincipal UUID userId, @RequestParam LocalDate date) {
        List<CalendarEventResult> results = getGoogleCalendarEventsUseCase.getEvents(userId, date);
        return ResponseEntity.ok(results.stream().map(CalendarEventResponse::from).toList());
    }

    private ResponseEntity<Void> redirectTo(String url) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    public record ConnectUrlResponse(String url) {
    }

    public record GoogleCalendarStatusResponse(boolean connected, String email) {
    }

    public record CalendarEventResponse(
            String id, String title, Instant start, Instant end, boolean allDay, String location, String htmlLink,
            boolean accepted) {
        static CalendarEventResponse from(CalendarEventResult result) {
            return new CalendarEventResponse(
                    result.id(), result.title(), result.start(), result.end(), result.allDay(), result.location(),
                    result.htmlLink(), result.accepted());
        }
    }
}
