package com.agenda.kanban.calendar.application.port.out;

import com.agenda.kanban.calendar.domain.model.GoogleCalendarConnection;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link GoogleCalendarConnection}. */
public interface GoogleCalendarConnectionRepositoryPort {

    Optional<GoogleCalendarConnection> findByUserId(UUID userId);

    GoogleCalendarConnection save(GoogleCalendarConnection connection);

    void deleteByUserId(UUID userId);
}
