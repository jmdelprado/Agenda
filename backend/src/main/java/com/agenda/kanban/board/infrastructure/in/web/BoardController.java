package com.agenda.kanban.board.infrastructure.in.web;

import com.agenda.kanban.board.application.port.in.CreateColumnUseCase;
import com.agenda.kanban.board.application.port.in.CreateColumnUseCase.ColumnResult;
import com.agenda.kanban.board.application.port.in.CreateColumnUseCase.CreateColumnCommand;
import com.agenda.kanban.board.application.port.in.DeleteColumnUseCase;
import com.agenda.kanban.board.application.port.in.DeleteColumnUseCase.DeleteColumnCommand;
import com.agenda.kanban.board.application.port.in.GetBoardUseCase;
import com.agenda.kanban.board.application.port.in.GetBoardUseCase.BoardView;
import com.agenda.kanban.board.application.port.in.GetBoardUseCase.GetBoardQuery;
import com.agenda.kanban.board.application.port.in.RenameColumnUseCase;
import com.agenda.kanban.board.application.port.in.RenameColumnUseCase.RenameColumnCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para el tablero y sus columnas (FR-002). */
@RestController
@RequestMapping("/api/v1")
public class BoardController {

    private final GetBoardUseCase getBoardUseCase;
    private final CreateColumnUseCase createColumnUseCase;
    private final RenameColumnUseCase renameColumnUseCase;
    private final DeleteColumnUseCase deleteColumnUseCase;

    public BoardController(
            GetBoardUseCase getBoardUseCase,
            CreateColumnUseCase createColumnUseCase,
            RenameColumnUseCase renameColumnUseCase,
            DeleteColumnUseCase deleteColumnUseCase) {
        this.getBoardUseCase = getBoardUseCase;
        this.createColumnUseCase = createColumnUseCase;
        this.renameColumnUseCase = renameColumnUseCase;
        this.deleteColumnUseCase = deleteColumnUseCase;
    }

    @GetMapping("/workspaces/{workspaceId}/board")
    public ResponseEntity<BoardView> getBoard(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(getBoardUseCase.getBoard(new GetBoardQuery(userId, workspaceId)));
    }

    @PostMapping("/workspaces/{workspaceId}/board/columns")
    public ResponseEntity<ColumnResult> createColumn(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateColumnRequest request) {
        ColumnResult result = createColumnUseCase.createColumn(
                new CreateColumnCommand(userId, workspaceId, request.name()));
        return ResponseEntity.created(URI.create("/api/v1/boards/columns/" + result.id())).body(result);
    }

    @PatchMapping("/boards/columns/{columnId}")
    public ResponseEntity<Void> renameColumn(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID columnId,
            @Valid @RequestBody RenameColumnRequest request) {
        renameColumnUseCase.renameColumn(new RenameColumnCommand(userId, columnId, request.name()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/boards/columns/{columnId}")
    public ResponseEntity<Void> deleteColumn(@AuthenticationPrincipal UUID userId, @PathVariable UUID columnId) {
        deleteColumnUseCase.deleteColumn(new DeleteColumnCommand(userId, columnId));
        return ResponseEntity.noContent().build();
    }

    public record CreateColumnRequest(@NotBlank @Size(min = 1, max = 50) String name) {
    }

    public record RenameColumnRequest(@NotBlank @Size(min = 1, max = 50) String name) {
    }
}
