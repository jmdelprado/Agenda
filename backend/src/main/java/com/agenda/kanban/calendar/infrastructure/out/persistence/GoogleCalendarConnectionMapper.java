package com.agenda.kanban.calendar.infrastructure.out.persistence;

import com.agenda.kanban.calendar.domain.model.GoogleCalendarConnection;

final class GoogleCalendarConnectionMapper {

    private GoogleCalendarConnectionMapper() {
    }

    static GoogleCalendarConnectionJpaEntity toJpaEntity(GoogleCalendarConnection connection) {
        return new GoogleCalendarConnectionJpaEntity(
                connection.getId(), connection.getUserId(), connection.getAccessToken(),
                connection.getRefreshToken(), connection.getTokenExpiresAt(), connection.getGoogleEmail(),
                connection.getConnectedAt());
    }

    static GoogleCalendarConnection toDomain(GoogleCalendarConnectionJpaEntity entity) {
        return new GoogleCalendarConnection(
                entity.getId(), entity.getUserId(), entity.getAccessToken(), entity.getRefreshToken(),
                entity.getTokenExpiresAt(), entity.getGoogleEmail(), entity.getConnectedAt());
    }
}
