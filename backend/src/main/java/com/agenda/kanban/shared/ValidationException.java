package com.agenda.kanban.shared;

/** Excepción de dominio para violaciones de invariantes de negocio (se traduce a HTTP 400). */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
