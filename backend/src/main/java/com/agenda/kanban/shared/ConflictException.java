package com.agenda.kanban.shared;

/** Excepción de dominio genérica para conflictos de negocio (se traduce a HTTP 409). */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
