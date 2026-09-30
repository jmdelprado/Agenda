package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ConflictException;
import com.agenda.kanban.task.application.port.in.MoveTaskUseCase;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mueve una tarjeta a otra columna (SC-002). Si la columna destino es la de mayor posición
 * del tablero (columna terminal, p.ej. "Hecho"), la tarea se marca como completada; si se
 * mueve a cualquier otra columna, se reabre (completedAt = null).
 */
@Service
public class MoveTaskService implements MoveTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public MoveTaskService(
            TaskRepositoryPort taskRepository,
            ColumnRepositoryPort columnRepository,
            BoardRepositoryPort boardRepository,
            WorkspaceRepositoryPort workspaceRepository) {
        this.taskRepository = taskRepository;
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public void moveTask(MoveTaskCommand command) {
        Task task = TaskAccess.requireOwnedTask(
                command.userId(), command.taskId(), taskRepository, columnRepository, boardRepository,
                workspaceRepository);
        Column sourceColumn = TaskAccess.requireOwnedColumn(
                command.userId(), task.getColumnId(), columnRepository, boardRepository, workspaceRepository);
        Column targetColumn = TaskAccess.requireOwnedColumn(
                command.userId(), command.targetColumnId(), columnRepository, boardRepository, workspaceRepository);

        if (!targetColumn.getBoardId().equals(sourceColumn.getBoardId())) {
            throw new ConflictException("No se puede mover una tarea a una columna de otro tablero");
        }

        List<Column> orderedColumns = columnRepository.findByBoardIdOrderByPosition(sourceColumn.getBoardId());
        Column terminalColumn = orderedColumns.get(orderedColumns.size() - 1);
        boolean completing = terminalColumn.getId().equals(targetColumn.getId());

        taskRepository.save(task.moveTo(targetColumn.getId(), completing, Instant.now()));
    }
}
