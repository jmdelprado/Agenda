package com.agenda.kanban.board.application.service;

import com.agenda.kanban.board.application.port.in.RenameColumnUseCase;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Renombra una columna existente (FR-002). */
@Service
public class RenameColumnService implements RenameColumnUseCase {

    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public RenameColumnService(
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public void renameColumn(RenameColumnCommand command) {
        Column column = WorkspaceAccess.requireOwnedColumn(
                command.userId(), command.columnId(), columnRepository, boardRepository, workspaceRepository);
        columnRepository.save(column.rename(command.newName()));
    }
}
