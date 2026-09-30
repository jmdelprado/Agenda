package com.agenda.kanban.auth.domain.exception;

import com.agenda.kanban.shared.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }
}
