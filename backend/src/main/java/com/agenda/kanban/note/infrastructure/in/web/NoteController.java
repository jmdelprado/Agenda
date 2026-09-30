package com.agenda.kanban.note.infrastructure.in.web;

import com.agenda.kanban.note.application.port.in.CreateChecklistItemUseCase;
import com.agenda.kanban.note.application.port.in.CreateChecklistItemUseCase.CreateChecklistItemCommand;
import com.agenda.kanban.note.application.port.in.GetChecklistUseCase;
import com.agenda.kanban.note.application.port.in.GetChecklistUseCase.ChecklistItemResult;
import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase;
import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase.DayNoteResult;
import com.agenda.kanban.note.application.port.in.SaveDayNoteUseCase;
import com.agenda.kanban.note.application.port.in.SaveDayNoteUseCase.SaveDayNoteCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para las notas de texto libre y la checklist rápida de la agenda de
 * papel (independientes de las tarjetas Kanban): "dejar escribir en la agenda, apuntar checklist
 * y demás". Ambos recursos se cargan en un único GET por espacio de trabajo y el frontend los
 * agrupa por día, igual que ya hace con {@code GET /workspaces/{id}/agenda}.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}")
public class NoteController {

    private final GetDayNotesUseCase getDayNotesUseCase;
    private final SaveDayNoteUseCase saveDayNoteUseCase;
    private final GetChecklistUseCase getChecklistUseCase;
    private final CreateChecklistItemUseCase createChecklistItemUseCase;

    public NoteController(
            GetDayNotesUseCase getDayNotesUseCase,
            SaveDayNoteUseCase saveDayNoteUseCase,
            GetChecklistUseCase getChecklistUseCase,
            CreateChecklistItemUseCase createChecklistItemUseCase) {
        this.getDayNotesUseCase = getDayNotesUseCase;
        this.saveDayNoteUseCase = saveDayNoteUseCase;
        this.getChecklistUseCase = getChecklistUseCase;
        this.createChecklistItemUseCase = createChecklistItemUseCase;
    }

    @GetMapping("/notes")
    public ResponseEntity<List<DayNoteResponse>> getNotes(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId) {
        List<DayNoteResult> results = getDayNotesUseCase.getNotes(userId, workspaceId);
        return ResponseEntity.ok(results.stream().map(DayNoteResponse::from).toList());
    }

    /**
     * PATCH en vez de PUT: la configuración CORS del backend (SecurityConfig) solo permite
     * GET/POST/PATCH/DELETE/OPTIONS. Semántica de upsert por fecha igual que la de un PUT.
     */
    @PatchMapping("/notes/{date}")
    public ResponseEntity<DayNoteResponse> saveNote(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID workspaceId,
            @PathVariable LocalDate date,
            @Valid @RequestBody SaveNoteRequest request) {
        DayNoteResult result =
                saveDayNoteUseCase.save(new SaveDayNoteCommand(userId, workspaceId, date, request.content()));
        return ResponseEntity.ok(DayNoteResponse.from(result));
    }

    @GetMapping("/checklist")
    public ResponseEntity<List<ChecklistItemResponse>> getChecklist(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID workspaceId) {
        List<ChecklistItemResult> results = getChecklistUseCase.getChecklist(userId, workspaceId);
        return ResponseEntity.ok(results.stream().map(ChecklistItemResponse::from).toList());
    }

    @PostMapping("/checklist")
    public ResponseEntity<ChecklistItemResponse> createChecklistItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateChecklistItemRequest request) {
        ChecklistItemResult result = createChecklistItemUseCase.create(
                new CreateChecklistItemCommand(userId, workspaceId, request.date(), request.text()));
        return ResponseEntity.created(URI.create("/api/v1/checklist/" + result.id()))
                .body(ChecklistItemResponse.from(result));
    }

    public record SaveNoteRequest(@Size(max = 5000) String content) {
    }

    public record CreateChecklistItemRequest(@NotNull LocalDate date, @NotBlank @Size(min = 1, max = 500) String text) {
    }

    public record DayNoteResponse(LocalDate date, String content) {
        static DayNoteResponse from(DayNoteResult result) {
            return new DayNoteResponse(result.date(), result.content());
        }
    }

    public record ChecklistItemResponse(UUID id, LocalDate date, String text, boolean done, int position) {
        static ChecklistItemResponse from(ChecklistItemResult result) {
            return new ChecklistItemResponse(
                    result.id(), result.date(), result.text(), result.done(), result.position());
        }
    }
}
