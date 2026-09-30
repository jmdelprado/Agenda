package com.agenda.kanban.board.application.service;

import com.agenda.kanban.board.application.port.in.DeleteColumnUseCase;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ConflictException;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Elimina una columna. Rechaza la operación (409) si es la única columna del tablero
 * (edge case de spec.md). Las tareas de la columna se eliminan en cascada a nivel de BD.
 */
@Service
public class DeleteColumnService implements DeleteColumnUseCase {

    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public DeleteColumnService(
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public void deleteColumn(DeleteColumnCommand command) {
        Column column = WorkspaceAccess.requireOwnedColumn(
                command.userId(), command.columnId(), columnRepository, boardRepository, workspaceRepository);

        if (columnRepository.countByBoardId(column.getBoardId()) <= 1) {
            throw new ConflictException("No se puede eliminar la única columna del tablero");
        }

        columnRepository.deleteById(column.getId());
    }
}
