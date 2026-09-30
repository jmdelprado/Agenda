package com.agenda.kanban.board.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Columna / etapa del flujo de trabajo (FR-002). POJO puro de dominio — ver plan.md
 * (Arquitectura Hexagonal). El invariante "no se puede eliminar la última columna del
 * tablero" requiere conocer a las columnas hermanas, así que se valida en la capa de
 * aplicación (DeleteColumnService), no aquí.
 */
public final class Column {

    private static final int NAME_MAX_LENGTH = 50;

    private final UUID id;
    private final UUID boardId;
    private final String name;
    private final int position;

    public Column(UUID id, UUID boardId, String name, int position) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.boardId = Objects.requireNonNull(boardId, "boardId es obligatorio");
        this.name = validateName(name);
        if (position < 0) {
            throw new ValidationException("La posición de la columna no puede ser negativa");
        }
        this.position = position;
    }

    public static Column create(UUID id, UUID boardId, String name, int position) {
        return new Column(id, boardId, name, position);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("El nombre de la columna es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new ValidationException(
                    "El nombre de la columna debe tener entre 1 y " + NAME_MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }

    public Column rename(String newName) {
        return new Column(id, boardId, newName, position);
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column column)) return false;
        return id.equals(column.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
