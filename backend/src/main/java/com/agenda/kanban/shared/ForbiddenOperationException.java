package com.agenda.kanban.shared;

/**
 * Excepción de dominio para operaciones sobre un recurso que no pertenece al usuario autenticado
 * (se traduce a HTTP 403). Usada para el aislamiento estricto entre cuentas y espacios de trabajo (FR-009).
 */
public class ForbiddenOperationException extends DomainException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
