package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.HandleGoogleCallbackUseCase;
import com.agenda.kanban.calendar.application.port.out.GoogleCalendarConnectionRepositoryPort;
import com.agenda.kanban.calendar.application.port.out.GoogleOAuthClientPort;
import com.agenda.kanban.calendar.application.port.out.GoogleOAuthClientPort.TokenResult;
import com.agenda.kanban.calendar.domain.model.GoogleCalendarConnection;
import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link HandleGoogleCallbackUseCase}. */
@Service
public class HandleGoogleCallbackService implements HandleGoogleCallbackUseCase {

    private final GoogleOAuthStateSigner stateSigner;
    private final GoogleOAuthClientPort oauthClient;
    private final GoogleCalendarConnectionRepositoryPort repository;

    public HandleGoogleCallbackService(
            GoogleOAuthStateSigner stateSigner, GoogleOAuthClientPort oauthClient,
            GoogleCalendarConnectionRepositoryPort repository) {
        this.stateSigner = stateSigner;
        this.oauthClient = oauthClient;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handleCallback(HandleGoogleCallbackCommand command) {
        UUID userId = stateSigner.verifyAndGetUserId(command.state());
        TokenResult tokens = oauthClient.exchangeCode(command.code());
        String email = safeFetchEmail(tokens.accessToken());
        Instant now = Instant.now();

        GoogleCalendarConnection existing = repository.findByUserId(userId).orElse(null);
        if (existing == null && tokens.refreshToken() == null) {
            throw new ValidationException("Google no devolvió un refresh token; vuelve a intentar la conexión");
        }

        GoogleCalendarConnection toSave = existing != null
                ? existing.withTokens(tokens.accessToken(), tokens.refreshToken(), tokens.expiresAt(), email)
                : GoogleCalendarConnection.create(
                        userId, tokens.accessToken(), tokens.refreshToken(), tokens.expiresAt(), email, now);
        repository.save(toSave);
    }

    private String safeFetchEmail(String accessToken) {
        try {
            return oauthClient.fetchEmail(accessToken);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
