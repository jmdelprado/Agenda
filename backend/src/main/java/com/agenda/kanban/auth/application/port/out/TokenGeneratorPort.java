package com.agenda.kanban.auth.application.port.out;

import java.util.UUID;

/** Puerto de salida: emisión y validación de tokens JWT. Implementado en infrastructure/out/security. */
public interface TokenGeneratorPort {

    String generateAccessToken(UUID userId, String email);

    String generateRefreshToken(UUID userId, String email);

    /**
     * Valida un refresh token y devuelve el id del usuario al que pertenece.
     *
     * @throws com.agenda.kanban.shared.UnauthorizedException si el token es inválido, ha expirado o no es de tipo refresh
     */
    UUID validateRefreshTokenAndGetUserId(String refreshToken);

    /**
     * Valida un access token (usado por el filtro de seguridad en cada petición) y devuelve el id del usuario.
     *
     * @throws com.agenda.kanban.shared.UnauthorizedException si el token es inválido, ha expirado o no es de tipo access
     */
    UUID validateAccessTokenAndGetUserId(String accessToken);
}
