package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.task.application.port.in.UpdateTaskUseCase;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Edita el título/descripción de una tarjeta existente. */
@Service
public class UpdateTaskService implements UpdateTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public UpdateTaskService(
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
    public void updateTask(UpdateTaskCommand command) {
        Task task = TaskAccess.requireOwnedTask(
                command.userId(), command.taskId(), taskRepository, columnRepository, boardRepository,
                workspaceRepository);
        taskRepository.save(task.edit(command.title(), command.description()));
    }
}
