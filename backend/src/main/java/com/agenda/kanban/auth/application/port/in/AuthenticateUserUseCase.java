package com.agenda.kanban.auth.application.port.in;

/** Puerto de entrada: caso de uso "iniciar sesión" (POST /auth/login). */
public interface AuthenticateUserUseCase {

    AuthTokens login(AuthenticateUserCommand command);

    record AuthenticateUserCommand(String email, String rawPassword) {
    }

    record AuthTokens(String accessToken, String refreshToken) {
    }
}
