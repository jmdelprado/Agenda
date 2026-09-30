package com.agenda.kanban.shared;

/** Excepción de dominio genérica para credenciales inválidas o token inválido/expirado (se traduce a HTTP 401). */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
