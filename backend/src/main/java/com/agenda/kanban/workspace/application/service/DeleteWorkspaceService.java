package com.agenda.kanban.workspace.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.reminder.application.port.in.CancelReminderUseCase;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.in.DeleteWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ver {@link DeleteWorkspaceUseCase}. Archiva el workspace (soft delete, FR-012) y cancela, vía
 * {@link CancelReminderUseCase}, el recordatorio PENDING de cada una de sus tareas (US3-AS3).
 */
@Service
public class DeleteWorkspaceService implements DeleteWorkspaceUseCase {

    private final WorkspaceRepositoryPort workspaceRepository;
    private final BoardRepositoryPort boardRepository;
    private final ColumnRepositoryPort columnRepository;
    private final TaskRepositoryPort taskRepository;
    private final CancelReminderUseCase cancelReminderUseCase;

    public DeleteWorkspaceService(
            WorkspaceRepositoryPort workspaceRepository,
            BoardRepositoryPort boardRepository,
            ColumnRepositoryPort columnRepository,
            TaskRepositoryPort taskRepository,
            CancelReminderUseCase cancelReminderUseCase) {
        this.workspaceRepository = workspaceRepository;
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
        this.taskRepository = taskRepository;
        this.cancelReminderUseCase = cancelReminderUseCase;
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(userId)) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }

        boardRepository.findByWorkspaceId(workspace.getId()).ifPresent(board -> cancelRemindersOfBoard(board));

        workspaceRepository.save(workspace.archive(Instant.now()));
    }

    private void cancelRemindersOfBoard(Board board) {
        for (Column column : columnRepository.findByBoardIdOrderByPosition(board.getId())) {
            for (Task task : taskRepository.findByColumnIdOrderByCreatedAt(column.getId())) {
                cancelReminderUseCase.cancelForTask(task.getId());
            }
        }
    }
}
