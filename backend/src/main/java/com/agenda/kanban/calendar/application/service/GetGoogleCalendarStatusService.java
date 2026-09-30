package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.GetGoogleCalendarStatusUseCase;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarConnectionRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link GetGoogleCalendarStatusUseCase}. */
@Service
public class GetGoogleCalendarStatusService implements GetGoogleCalendarStatusUseCase {

    private final GoogleCalendarConnectionRepositoryPort repository;

    public GetGoogleCalendarStatusService(GoogleCalendarConnectionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public GoogleCalendarStatusResult getStatus(UUID userId) {
        return repository.findByUserId(userId)
                .map(connection -> new GoogleCalendarStatusResult(true, connection.getGoogleEmail()))
                .orElse(new GoogleCalendarStatusResult(false, null));
    }
}
