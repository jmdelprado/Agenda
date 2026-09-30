package com.agenda.kanban.assistant.application.port.out;

import java.util.List;

/** Puerto de salida: pide a un modelo de IA que extraiga elementos accionables de una nota de reunión. */
public interface AiExtractionClientPort {

    ExtractionResult extract(String meetingTitle, String noteContent);

    /** {@code summary} puede venir vacío si la IA no considera que haya nada que añadir a la nota del día. */
    record ExtractionResult(List<String> tasks, List<String> checklistItems, String summary) {
    }
}
