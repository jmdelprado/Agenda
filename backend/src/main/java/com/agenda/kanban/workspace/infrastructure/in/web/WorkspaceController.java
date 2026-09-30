package com.agenda.kanban.workspace.infrastructure.in.web;

import com.agenda.kanban.workspace.application.port.in.CreateWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.in.CreateWorkspaceUseCase.CreateWorkspaceCommand;
import com.agenda.kanban.workspace.application.port.in.CreateWorkspaceUseCase.CreateWorkspaceResult;
import com.agenda.kanban.workspace.application.port.in.DeleteWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.in.ListWorkspacesUseCase;
import com.agenda.kanban.workspace.application.port.in.ListWorkspacesUseCase.WorkspaceSummary;
import com.agenda.kanban.workspace.application.port.in.RenameWorkspaceUseCase;
import com.agenda.kanban.workspace.application.port.in.RenameWorkspaceUseCase.RenameWorkspaceCommand;
import com.agenda.kanban.workspace.application.port.in.RenameWorkspaceUseCase.RenameWorkspaceResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para espacios de trabajo: crear (User Story 1) y listar/renombrar/
 * eliminar (User Story 3, T063), todo bajo el usuario autenticado (aislamiento FR-009).
 */
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final ListWorkspacesUseCase listWorkspacesUseCase;
    private final RenameWorkspaceUseCase renameWorkspaceUseCase;
    private final DeleteWorkspaceUseCase deleteWorkspaceUseCase;

    public WorkspaceController(
            CreateWorkspaceUseCase createWorkspaceUseCase,
            ListWorkspacesUseCase listWorkspacesUseCase,
            RenameWorkspaceUseCase renameWorkspaceUseCase,
            DeleteWorkspaceUseCase deleteWorkspaceUseCase) {
        this.createWorkspaceUseCase = createWorkspaceUseCase;
        this.listWorkspacesUseCase = listWorkspacesUseCase;
        this.renameWorkspaceUseCase = renameWorkspaceUseCase;
        this.deleteWorkspaceUseCase = deleteWorkspaceUseCase;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(
            @AuthenticationPrincipal UUID userId, @Valid @RequestBody CreateWorkspaceRequest request) {
        CreateWorkspaceResult result =
                createWorkspaceUseCase.createWorkspace(new CreateWorkspaceCommand(userId, request.name()));
        return ResponseEntity.created(URI.create("/api/v1/workspaces/" + result.workspaceId()))
                .body(WorkspaceResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceSummaryResponse>> list(@AuthenticationPrincipal UUID userId) {
        List<WorkspaceSummary> summaries = listWorkspacesUseCase.listWorkspaces(userId);
        return ResponseEntity.ok(summaries.stream().map(WorkspaceSummaryResponse::from).toList());
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<RenameWorkspaceResponse> rename(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId,
            @Valid @RequestBody RenameWorkspaceRequest request) {
        RenameWorkspaceResult result = renameWorkspaceUseCase.rename(
                new RenameWorkspaceCommand(userId, workspaceId, request.name()));
        return ResponseEntity.ok(RenameWorkspaceResponse.from(result));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId) {
        deleteWorkspaceUseCase.delete(userId, workspaceId);
        return ResponseEntity.noContent().build();
    }

    public record CreateWorkspaceRequest(@NotBlank @Size(min = 1, max = 100) String name) {
    }

    public record RenameWorkspaceRequest(@NotBlank @Size(min = 1, max = 100) String name) {
    }

    public record WorkspaceResponse(UUID id, String name, Instant createdAt, BoardResponse board) {
        static WorkspaceResponse from(CreateWorkspaceResult result) {
            return new WorkspaceResponse(
                    result.workspaceId(), result.name(), result.createdAt(),
                    new BoardResponse(result.boardId(), result.columns()));
        }
    }

    public record WorkspaceSummaryResponse(UUID id, String name, Instant createdAt) {
        static WorkspaceSummaryResponse from(WorkspaceSummary summary) {
            return new WorkspaceSummaryResponse(summary.id(), summary.name(), summary.createdAt());
        }
    }

    public record RenameWorkspaceResponse(UUID id, String name) {
        static RenameWorkspaceResponse from(RenameWorkspaceResult result) {
            return new RenameWorkspaceResponse(result.workspaceId(), result.name());
        }
    }

    public record BoardResponse(UUID id, List<CreateWorkspaceUseCase.ColumnSummary> columns) {
    }
}
