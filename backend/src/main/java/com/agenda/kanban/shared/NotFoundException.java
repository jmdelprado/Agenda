package com.agenda.kanban.shared;

/** Excepción de dominio genérica para "recurso no encontrado" (se traduce a HTTP 404). */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
