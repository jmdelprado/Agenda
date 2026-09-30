package com.agenda.kanban.workspace.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ConflictException;
import com.agenda.kanban.workspace.application.port.in.CreateWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea, de forma atómica, un Workspace nuevo junto con su Board y las 3 columnas por defecto
 * ("Por hacer"/"En progreso"/"Hecho") — ver data-model.md § Workspace y Assumptions de spec.md.
 */
@Service
public class CreateWorkspaceService implements CreateWorkspaceUseCase {

    private static final List<String> DEFAULT_COLUMN_NAMES = List.of("Por hacer", "En progreso", "Hecho");

    private final WorkspaceRepositoryPort workspaceRepository;
    private final BoardRepositoryPort boardRepository;
    private final ColumnRepositoryPort columnRepository;

    public CreateWorkspaceService(
            WorkspaceRepositoryPort workspaceRepository,
            BoardRepositoryPort boardRepository,
            ColumnRepositoryPort columnRepository) {
        this.workspaceRepository = workspaceRepository;
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
    }

    @Override
    @Transactional
    public CreateWorkspaceResult createWorkspace(CreateWorkspaceCommand command) {
        if (workspaceRepository.existsActiveByUserIdAndName(command.userId(), command.name())) {
            throw new ConflictException(
                    "Ya existe un espacio de trabajo con el nombre \"" + command.name() + "\"");
        }

        Instant now = Instant.now();
        Workspace workspace = workspaceRepository.save(
                Workspace.create(UUID.randomUUID(), command.userId(), command.name(), now));

        Board board = boardRepository.save(Board.create(UUID.randomUUID(), workspace.getId()));

        List<ColumnSummary> columnSummaries = createDefaultColumns(board.getId());

        return new CreateWorkspaceResult(
                workspace.getId(), workspace.getName(), workspace.getCreatedAt(), board.getId(), columnSummaries);
    }

    private List<ColumnSummary> createDefaultColumns(UUID boardId) {
        return DEFAULT_COLUMN_NAMES.stream()
                .map(name -> {
                    int position = DEFAULT_COLUMN_NAMES.indexOf(name);
                    Column saved = columnRepository.save(Column.create(UUID.randomUUID(), boardId, name, position));
                    return new ColumnSummary(saved.getId(), saved.getName(), saved.getPosition());
                })
                .toList();
    }
}
