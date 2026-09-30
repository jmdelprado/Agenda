package com.agenda.kanban.calendar.application.port.out;

import java.time.Instant;

/** Puerto de salida: intercambio de código/refresco de tokens y consulta del email, contra Google OAuth. */
public interface GoogleOAuthClientPort {

    TokenResult exchangeCode(String code);

    TokenResult refreshToken(String refreshToken);

    /** Best-effort: devuelve null si no se pudo obtener (nunca lanza). */
    String fetchEmail(String accessToken);

    /** {@code refreshToken} puede venir nulo (Google no siempre lo reenvía al refrescar). */
    record TokenResult(String accessToken, String refreshToken, Instant expiresAt) {
    }
}
