package com.agenda.kanban.assistant.domain.exception;

/** Fallo al llamar a la API de IA o al interpretar su respuesta. */
public class AiExtractionException extends RuntimeException {

    public AiExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
