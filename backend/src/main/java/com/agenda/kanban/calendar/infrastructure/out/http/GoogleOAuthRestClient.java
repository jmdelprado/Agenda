package com.agenda.kanban.calendar.infrastructure.out.http;

import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarEventsUseCase.CalendarEventResult;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarApiPort;
import com.agenda.kanban.calendar.application.port.out.GoogleOAuthClientPort;
import com.agenda.kanban.calendar.domain.exception.GoogleCalendarIntegrationException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Implementa {@link GoogleOAuthClientPort} y {@link GoogleCalendarApiPort} con peticiones HTTP directas a Google. */
@Component
public class GoogleOAuthRestClient implements GoogleOAuthClientPort, GoogleCalendarApiPort {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthRestClient(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret}") String clientSecret,
            @Value("${app.google.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public TokenResult exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        return toTokenResult(postToken(form));
    }

    @Override
    public TokenResult refreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return toTokenResult(postToken(form));
    }

    @Override
    public String fetchEmail(String accessToken) {
        try {
            GoogleUserInfoResponse info = restClient.get()
                    .uri(USERINFO_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleUserInfoResponse.class);
            return info != null ? info.email() : null;
        } catch (RestClientException ex) {
            return null;
        }
    }

    @Override
    public List<CalendarEventResult> listEvents(String accessToken, Instant timeMin, Instant timeMax) {
        try {
            GoogleEventsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/calendar/v3/calendars/primary/events")
                            .queryParam("timeMin", timeMin.toString())
                            .queryParam("timeMax", timeMax.toString())
                            .queryParam("singleEvents", "true")
                            .queryParam("orderBy", "startTime")
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleEventsResponse.class);
            if (response == null || response.items() == null) {
                return List.of();
            }
            return response.items().stream().map(this::toEventResult).filter(Objects::nonNull).toList();
        } catch (RestClientException ex) {
            throw new GoogleCalendarIntegrationException("No se pudieron obtener los eventos de Google Calendar", ex);
        }
    }

    private GoogleTokenResponse postToken(MultiValueMap<String, String> form) {
        try {
            return restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (RestClientException ex) {
            throw new GoogleCalendarIntegrationException("No se pudo obtener el token de Google", ex);
        }
    }

    private TokenResult toTokenResult(GoogleTokenResponse response) {
        if (response == null || response.accessToken() == null) {
            throw new GoogleCalendarIntegrationException("Respuesta de Google sin access_token", null);
        }
        int expiresInSeconds = response.expiresIn() != null ? response.expiresIn() : 3600;
        Instant expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        return new TokenResult(response.accessToken(), response.refreshToken(), expiresAt);
    }

    private CalendarEventResult toEventResult(GoogleEventItem item) {
        if (item.start() == null || item.end() == null) {
            return null;
        }
        boolean allDay = item.start().date() != null;
        Instant start = allDay ? startOfDay(item.start().date()) : Instant.parse(item.start().dateTime());
        Instant end = allDay ? startOfDay(item.end().date()) : Instant.parse(item.end().dateTime());
        String title = item.summary() != null ? item.summary() : "(Sin título)";
        return new CalendarEventResult(
                item.id(), title, start, end, allDay, item.location(), item.htmlLink(), isAccepted(item.attendees()));
    }

    /** Si el evento no tiene invitados (evento propio) no hay nada que aceptar: se trata como aceptado. */
    private boolean isAccepted(List<GoogleEventAttendee> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            return true;
        }
        return attendees.stream()
                .filter(attendee -> Boolean.TRUE.equals(attendee.self()))
                .findFirst()
                .map(attendee -> "accepted".equals(attendee.responseStatus()))
                .orElse(true);
    }

    private Instant startOfDay(String isoDate) {
        return LocalDate.parse(isoDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") Integer expiresIn) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleUserInfoResponse(String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleEventsResponse(List<GoogleEventItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleEventItem(
            String id, String summary, String location, String htmlLink,
            GoogleEventDateTime start, GoogleEventDateTime end, List<GoogleEventAttendee> attendees) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleEventDateTime(String date, String dateTime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleEventAttendee(String email, Boolean self, String responseStatus) {
    }
}
