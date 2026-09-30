package com.agenda.kanban.task.infrastructure.in.web;

import com.agenda.kanban.task.application.port.in.GetWorkspaceAgendaUseCase;
import com.agenda.kanban.task.application.port.in.GetWorkspaceAgendaUseCase.AgendaTaskResult;
import com.agenda.kanban.task.application.port.in.GetWorkspaceAgendaUseCase.GetWorkspaceAgendaQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la agenda por espacio de trabajo (FR-004).
 */
@RestController
@RequestMapping("/api/v1")
public class AgendaController {

    private final GetWorkspaceAgendaUseCase getWorkspaceAgendaUseCase;

    public AgendaController(GetWorkspaceAgendaUseCase getWorkspaceAgendaUseCase) {
        this.getWorkspaceAgendaUseCase = getWorkspaceAgendaUseCase;
    }

    @GetMapping("/workspaces/{workspaceId}/agenda")
    public ResponseEntity<List<AgendaTaskResponse>> getWorkspaceAgenda(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId) {
        List<AgendaTaskResult> results =
                getWorkspaceAgendaUseCase.getAgenda(new GetWorkspaceAgendaQuery(userId, workspaceId));
        return ResponseEntity.ok(results.stream().map(AgendaTaskResponse::from).toList());
    }

    public record AgendaTaskResponse(UUID taskId, String title, Instant dueAt, boolean overdue, boolean completed) {
        static AgendaTaskResponse from(AgendaTaskResult result) {
            return new AgendaTaskResponse(
                    result.taskId(), result.title(), result.dueAt(), result.overdue(), result.completed());
        }
    }
}
