package com.agenda.kanban.calendar.infrastructure.out.persistence;

import com.agenda.kanban.calendar.application.port.out.GoogleCalendarConnectionRepositoryPort;
import com.agenda.kanban.calendar.domain.model.GoogleCalendarConnection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarConnectionRepositoryAdapter implements GoogleCalendarConnectionRepositoryPort {

    private final SpringDataGoogleCalendarConnectionJpaRepository jpaRepository;

    public GoogleCalendarConnectionRepositoryAdapter(SpringDataGoogleCalendarConnectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<GoogleCalendarConnection> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(GoogleCalendarConnectionMapper::toDomain);
    }

    @Override
    public GoogleCalendarConnection save(GoogleCalendarConnection connection) {
        GoogleCalendarConnectionJpaEntity saved =
                jpaRepository.save(GoogleCalendarConnectionMapper.toJpaEntity(connection));
        return GoogleCalendarConnectionMapper.toDomain(saved);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
