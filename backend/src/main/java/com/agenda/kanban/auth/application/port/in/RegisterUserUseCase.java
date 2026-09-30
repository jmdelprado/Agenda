package com.agenda.kanban.auth.application.port.in;

import java.util.UUID;

/** Puerto de entrada: caso de uso "registrar cuenta" (POST /auth/register). */
public interface RegisterUserUseCase {

    UUID register(RegisterUserCommand command);

    record RegisterUserCommand(String email, String rawPassword) {
    }
}
