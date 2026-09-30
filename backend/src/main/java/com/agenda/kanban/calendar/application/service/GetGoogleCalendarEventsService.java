package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarEventsUseCase;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarApiPort;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarConnectionRepositoryPort;
import com.agenda.kanban.calendar.application.port.out.GoogleOAuthClientPort;
import com.agenda.kanban.calendar.application.port.out.GoogleOAuthClientPort.TokenResult;
import com.agenda.kanban.calendar.domain.model.GoogleCalendarConnection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ver {@link GetGoogleCalendarEventsUseCase}. Refresca el access token en caliente si ha caducado
 * y, si el refresco falla (p.ej. el usuario revocó el acceso desde Google), da por perdida la
 * conexión y la borra en vez de propagar el error — el panel de "Reuniones" simplemente vuelve al
 * estado "no conectado".
 */
@Service
public class GetGoogleCalendarEventsService implements GetGoogleCalendarEventsUseCase {

    private final GoogleCalendarConnectionRepositoryPort repository;
    private final GoogleOAuthClientPort oauthClient;
    private final GoogleCalendarApiPort calendarApi;

    public GetGoogleCalendarEventsService(
            GoogleCalendarConnectionRepositoryPort repository, GoogleOAuthClientPort oauthClient,
            GoogleCalendarApiPort calendarApi) {
        this.repository = repository;
        this.oauthClient = oauthClient;
        this.calendarApi = calendarApi;
    }

    @Override
    @Transactional
    public List<CalendarEventResult> getEvents(UUID userId, LocalDate date) {
        GoogleCalendarConnection connection = repository.findByUserId(userId).orElse(null);
        if (connection == null) {
            return List.of();
        }

        Instant now = Instant.now();
        if (connection.isAccessTokenExpired(now)) {
            connection = refresh(connection);
            if (connection == null) {
                return List.of();
            }
        }

        ZoneId zone = ZoneId.systemDefault();
        Instant timeMin = date.atStartOfDay(zone).toInstant();
        Instant timeMax = date.plusDays(1).atStartOfDay(zone).toInstant();
        try {
            return calendarApi.listEvents(connection.getAccessToken(), timeMin, timeMax);
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    private GoogleCalendarConnection refresh(GoogleCalendarConnection connection) {
        try {
            TokenResult refreshed = oauthClient.refreshToken(connection.getRefreshToken());
            GoogleCalendarConnection updated = connection.withTokens(
                    refreshed.accessToken(), refreshed.refreshToken(), refreshed.expiresAt(), null);
            return repository.save(updated);
        } catch (RuntimeException ex) {
            repository.deleteByUserId(connection.getUserId());
            return null;
        }
    }
}
