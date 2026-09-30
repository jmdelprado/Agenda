package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.DisconnectGoogleCalendarUseCase;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarConnectionRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link DisconnectGoogleCalendarUseCase}. */
@Service
public class DisconnectGoogleCalendarService implements DisconnectGoogleCalendarUseCase {

    private final GoogleCalendarConnectionRepositoryPort repository;

    public DisconnectGoogleCalendarService(GoogleCalendarConnectionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void disconnect(UUID userId) {
        repository.deleteByUserId(userId);
    }
}
