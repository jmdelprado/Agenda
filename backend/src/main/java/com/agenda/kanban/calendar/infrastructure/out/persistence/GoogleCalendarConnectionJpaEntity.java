package com.agenda.kanban.calendar.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Mapea la tabla {@code google_calendar_connections} (una fila por usuario). */
@Entity
@Table(name = "google_calendar_connections")
public class GoogleCalendarConnectionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "token_expires_at", nullable = false)
    private Instant tokenExpiresAt;

    @Column(name = "google_email")
    private String googleEmail;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    protected GoogleCalendarConnectionJpaEntity() {
        // requerido por JPA
    }

    public GoogleCalendarConnectionJpaEntity(UUID id, UUID userId, String accessToken, String refreshToken,
            Instant tokenExpiresAt, String googleEmail, Instant connectedAt) {
        this.id = id;
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.googleEmail = googleEmail;
        this.connectedAt = connectedAt;
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
}
