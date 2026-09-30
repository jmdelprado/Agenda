package com.agenda.kanban.shared;

/** Límite de tasa excedido, p.ej. intentos de login (T073) (se traduce a HTTP 429). */
public class TooManyRequestsException extends DomainException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
