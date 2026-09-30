package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.task.application.port.in.CreateTaskUseCase;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea una tarjeta nueva en una columna (FR-001). */
@Service
public class CreateTaskService implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public CreateTaskService(
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
    public TaskResult createTask(CreateTaskCommand command) {
        var column = TaskAccess.requireOwnedColumn(
                command.userId(), command.columnId(), columnRepository, boardRepository, workspaceRepository);

        Task saved = taskRepository.save(
                Task.create(UUID.randomUUID(), column.getId(), command.title(), command.description()));

        return new TaskResult(saved.getId(), saved.getColumnId(), saved.getTitle(), saved.getDescription(),
                saved.isCompleted());
    }
}
