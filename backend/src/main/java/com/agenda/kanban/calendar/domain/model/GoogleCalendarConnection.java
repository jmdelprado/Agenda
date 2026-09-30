package com.agenda.kanban.calendar.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Conexión de un usuario con su Google Calendar: una por usuario, guarda el par de tokens OAuth
 * (access/refresh) necesarios para listar sus eventos sin volver a pedirle consentimiento.
 */
public final class GoogleCalendarConnection {

    private final UUID id;
    private final UUID userId;
    private final String accessToken;
    private final String refreshToken;
    private final Instant tokenExpiresAt;
    private final String googleEmail;
    private final Instant connectedAt;

    public GoogleCalendarConnection(UUID id, UUID userId, String accessToken, String refreshToken,
            Instant tokenExpiresAt, String googleEmail, Instant connectedAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken es obligatorio");
        this.refreshToken = Objects.requireNonNull(refreshToken, "refreshToken es obligatorio");
        this.tokenExpiresAt = Objects.requireNonNull(tokenExpiresAt, "tokenExpiresAt es obligatorio");
        this.googleEmail = googleEmail;
        this.connectedAt = Objects.requireNonNull(connectedAt, "connectedAt es obligatorio");
    }

    public static GoogleCalendarConnection create(
            UUID userId, String accessToken, String refreshToken, Instant tokenExpiresAt, String googleEmail,
            Instant now) {
        return new GoogleCalendarConnection(UUID.randomUUID(), userId, accessToken, refreshToken, tokenExpiresAt,
                googleEmail, now);
    }

    /** Reemplaza los tokens (reconexión o refresco). Si el nuevo refresh token o email es nulo, conserva el actual. */
    public GoogleCalendarConnection withTokens(
            String newAccessToken, String newRefreshToken, Instant newTokenExpiresAt, String newGoogleEmail) {
        return new GoogleCalendarConnection(
                id, userId, newAccessToken,
                newRefreshToken != null ? newRefreshToken : this.refreshToken,
                newTokenExpiresAt,
                newGoogleEmail != null ? newGoogleEmail : this.googleEmail,
                connectedAt);
    }

    public boolean isAccessTokenExpired(Instant now) {
        return !tokenExpiresAt.isAfter(now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoogleCalendarConnection that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
