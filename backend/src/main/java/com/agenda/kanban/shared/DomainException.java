package com.agenda.kanban.shared;

/**
 * Base de las excepciones de dominio. La capa de infraestructura (GlobalExceptionHandler)
 * es responsable de traducirlas a respuestas HTTP; el dominio y la aplicación no conocen HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
