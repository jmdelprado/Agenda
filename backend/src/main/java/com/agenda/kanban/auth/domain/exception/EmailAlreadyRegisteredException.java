package com.agenda.kanban.auth.domain.exception;

import com.agenda.kanban.shared.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {

    public EmailAlreadyRegisteredException(String email) {
        super("Ya existe una cuenta registrada con el email: " + email);
    }
}
