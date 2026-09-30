package com.agenda.kanban.board.application.port.out;

import com.agenda.kanban.board.domain.model.Board;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Board}. Implementado en infrastructure/out/persistence. */
public interface BoardRepositoryPort {

    Board save(Board board);

    Optional<Board> findById(UUID id);

    Optional<Board> findByWorkspaceId(UUID workspaceId);
}
