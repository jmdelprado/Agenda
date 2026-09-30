package com.agenda.kanban.auth.application.port.in;

/** Puerto de entrada: caso de uso "renovar access token" (POST /auth/refresh). */
public interface RefreshTokenUseCase {

    RefreshedAccessToken refresh(RefreshTokenCommand command);

    record RefreshTokenCommand(String refreshToken) {
    }

    /**
     * {@code refreshToken} es uno NUEVO (rotación): el usado en la petición queda implícitamente
     * obsoleto para el cliente, que debe sustituirlo por este. Al ser JWT sin estado no hay lista
     * de revocación server-side — la rotación reduce la ventana de reutilización de un token
     * filtrado, pero no permite invalidar el anterior antes de su expiración natural (T073).
     */
    record RefreshedAccessToken(String accessToken, String refreshToken) {
    }
}
