package com.agenda.kanban.calendar.application.port.in;

import java.util.UUID;

/** Puerto de entrada: construye la URL de consentimiento de Google a la que redirigir al usuario. */
public interface GetGoogleAuthUrlUseCase {

    String getAuthUrl(UUID userId);
}
