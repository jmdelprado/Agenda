package com.agenda.kanban.note.infrastructure.in.web;

import com.agenda.kanban.note.application.port.in.DeleteChecklistItemUseCase;
import com.agenda.kanban.note.application.port.in.DeleteChecklistItemUseCase.DeleteChecklistItemCommand;
import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import com.agenda.kanban.note.application.port.in.UpdateChecklistItemUseCase;
import com.agenda.kanban.note.application.port.in.UpdateChecklistItemUseCase.UpdateChecklistItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para editar/eliminar un elemento de checklist ya creado, por id. */
@RestController
@RequestMapping("/api/v1/checklist")
public class ChecklistItemController {

    private final UpdateChecklistItemUseCase updateChecklistItemUseCase;
    private final DeleteChecklistItemUseCase deleteChecklistItemUseCase;

    public ChecklistItemController(
            UpdateChecklistItemUseCase updateChecklistItemUseCase,
            DeleteChecklistItemUseCase deleteChecklistItemUseCase) {
        this.updateChecklistItemUseCase = updateChecklistItemUseCase;
        this.deleteChecklistItemUseCase = deleteChecklistItemUseCase;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ChecklistItemResponse> update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateChecklistItemRequest request) {
        ChecklistItemResult result = updateChecklistItemUseCase.update(
                new UpdateChecklistItemCommand(userId, itemId, request.done(), request.text()));
        return ResponseEntity.ok(ChecklistItemResponse.from(result));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID itemId) {
        deleteChecklistItemUseCase.delete(new DeleteChecklistItemCommand(userId, itemId));
        return ResponseEntity.noContent().build();
    }

    public record UpdateChecklistItemRequest(Boolean done, @Size(min = 1, max = 500) String text) {
    }

    public record ChecklistItemResponse(UUID id, LocalDate date, String text, boolean done, int position) {
        static ChecklistItemResponse from(ChecklistItemResult result) {
            return new ChecklistItemResponse(
                    result.id(), result.date(), result.text(), result.done(), result.position());
        }
    }
}
