package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.calendar.application.port.in.GetGoogleAuthUrlUseCase;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/** Ver {@link GetGoogleAuthUrlUseCase}. */
@Service
public class GetGoogleAuthUrlService implements GetGoogleAuthUrlUseCase {

    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";

    private final GoogleOAuthStateSigner stateSigner;
    private final String clientId;
    private final String redirectUri;
    private final String scope;

    public GetGoogleAuthUrlService(
            GoogleOAuthStateSigner stateSigner,
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.redirect-uri}") String redirectUri,
            @Value("${app.google.scope}") String scope) {
        this.stateSigner = stateSigner;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    @Override
    public String getAuthUrl(UUID userId) {
        String state = stateSigner.sign(userId);
        return UriComponentsBuilder.fromUriString(AUTH_ENDPOINT)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("scope", scope)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }
}
