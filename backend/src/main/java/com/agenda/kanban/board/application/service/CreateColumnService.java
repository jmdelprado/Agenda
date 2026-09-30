package com.agenda.kanban.board.application.service;

import com.agenda.kanban.board.application.port.in.CreateColumnUseCase;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea una columna nueva al final del tablero (FR-002). */
@Service
public class CreateColumnService implements CreateColumnUseCase {

    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public CreateColumnService(
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public ColumnResult createColumn(CreateColumnCommand command) {
        Board board = WorkspaceAccess.requireOwnedBoard(
                command.userId(), command.workspaceId(), boardRepository, workspaceRepository);

        int nextPosition = columnRepository.countByBoardId(board.getId());
        Column saved = columnRepository.save(
                Column.create(UUID.randomUUID(), board.getId(), command.name(), nextPosition));

        return new ColumnResult(saved.getId(), saved.getName(), saved.getPosition());
    }
}
