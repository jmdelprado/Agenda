package com.agenda.kanban.task.application.service;

import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Board;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.task.application.port.in.GetWorkspaceAgendaUseCase;
import com.agenda.kanban.task.application.port.out.TaskRepositoryPort;
import com.agenda.kanban.task.domain.model.Task;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link GetWorkspaceAgendaUseCase}. */
@Service
public class GetWorkspaceAgendaService implements GetWorkspaceAgendaUseCase {

    private final WorkspaceRepositoryPort workspaceRepository;
    private final BoardRepositoryPort boardRepository;
    private final ColumnRepositoryPort columnRepository;
    private final TaskRepositoryPort taskRepository;

    public GetWorkspaceAgendaService(
            WorkspaceRepositoryPort workspaceRepository,
            BoardRepositoryPort boardRepository,
            ColumnRepositoryPort columnRepository,
            TaskRepositoryPort taskRepository) {
        this.workspaceRepository = workspaceRepository;
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgendaTaskResult> getAgenda(GetWorkspaceAgendaQuery query) {
        Workspace workspace = workspaceRepository.findById(query.workspaceId())
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(query.userId())) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }
        Board board = boardRepository.findByWorkspaceId(workspace.getId())
                .orElseThrow(() -> new NotFoundException("Tablero no encontrado"));

        Instant now = Instant.now();
        List<Column> columns = columnRepository.findByBoardIdOrderByPosition(board.getId());
        return columns.stream()
                .flatMap(column -> taskRepository.findByColumnIdOrderByCreatedAt(column.getId()).stream())
                .filter(task -> task.getDueAt() != null)
                .sorted(Comparator.comparing(Task::getDueAt))
                .map(task -> new AgendaTaskResult(
                        task.getId(), task.getTitle(), task.getDueAt(), task.isOverdue(now), task.isCompleted()))
                .toList();
    }
}
