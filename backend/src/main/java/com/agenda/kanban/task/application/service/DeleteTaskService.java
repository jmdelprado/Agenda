package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.task.application.port.in.DeleteTaskUseCase;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Elimina una tarjeta. */
@Service
public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepository;
    private final ColumnRepositoryPort columnRepository;
    private final BoardRepositoryPort boardRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public DeleteTaskService(
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
    public void deleteTask(DeleteTaskCommand command) {
        Task task = TaskAccess.requireOwnedTask(
                command.userId(), command.taskId(), taskRepository, columnRepository, boardRepository,
                workspaceRepository);
        taskRepository.deleteById(task.getId());
    }
}
