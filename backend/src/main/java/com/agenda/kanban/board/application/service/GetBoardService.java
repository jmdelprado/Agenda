package com.agenda.kanban.board.application.service;

import com.agenda.kanban.board.application.port.in.GetBoardUseCase;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Obtiene el tablero completo de un workspace: columnas ordenadas por posición y sus tareas. */
@Service
public class GetBoardService implements GetBoardUseCase {

    private final BoardRepositoryPort boardRepository;
    private final ColumnRepositoryPort columnRepository;
    private final TaskRepositoryPort taskRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public GetBoardService(
            BoardRepositoryPort boardRepository,
            ColumnRepositoryPort columnRepository,
            TaskRepositoryPort taskRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
        this.taskRepository = taskRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardView getBoard(GetBoardQuery query) {
        Board board = WorkspaceAccess.requireOwnedBoard(
                query.userId(), query.workspaceId(), boardRepository, workspaceRepository);

        var columns = columnRepository.findByBoardIdOrderByPosition(board.getId()).stream()
                .map(this::toColumnView)
                .toList();

        return new BoardView(board.getId(), columns);
    }

    private ColumnView toColumnView(Column column) {
        var tasks = taskRepository.findByColumnIdOrderByCreatedAt(column.getId()).stream()
                .map(this::toTaskView)
                .toList();
        return new ColumnView(column.getId(), column.getName(), column.getPosition(), tasks);
    }

    private TaskView toTaskView(Task task) {
        Instant now = Instant.now();
        return new TaskView(
                task.getId(), task.getTitle(), task.getDescription(), task.isCompleted(),
                task.getDueAt(), task.getReminderLeadMinutes(), task.isOverdue(now));
    }
}
