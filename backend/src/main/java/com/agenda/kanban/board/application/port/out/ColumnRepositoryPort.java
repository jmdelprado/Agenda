package com.agenda.kanban.board.application.port.out;

import com.agenda.kanban.board.domain.model.Column;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Column}. Implementado en infrastructure/out/persistence. */
public interface ColumnRepositoryPort {

    Column save(Column column);

    Optional<Column> findById(UUID id);

    /** Columnas del tablero ordenadas por posición ascendente; la última es la columna terminal. */
    List<Column> findByBoardIdOrderByPosition(UUID boardId);

    int countByBoardId(UUID boardId);

    void deleteById(UUID id);
}
