package com.agenda.kanban.assistant.infrastructure.in.web;

import com.agenda.kanban.assistant.application.port.in.GenerateFromMeetingNoteUseCase;
import com.agenda.kanban.assistant.application.port.in.GenerateFromMeetingNoteUseCase.GenerateCommand;
import com.agenda.kanban.assistant.application.port.in.GenerateFromMeetingNoteUseCase.GenerateResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST del botón "Generar con IA" de una reunión. */
@RestController
@RequestMapping("/api/v1/calendar/google/notes")
public class AssistantController {

    private final GenerateFromMeetingNoteUseCase generateFromMeetingNoteUseCase;

    public AssistantController(GenerateFromMeetingNoteUseCase generateFromMeetingNoteUseCase) {
        this.generateFromMeetingNoteUseCase = generateFromMeetingNoteUseCase;
    }

    @PostMapping("/{eventId}/generate")
    public ResponseEntity<GenerateResponse> generate(
            @AuthenticationPrincipal UUID userId,
            @PathVariable String eventId,
            @Valid @RequestBody GenerateRequest request) {
        GenerateResult result = generateFromMeetingNoteUseCase.generate(
                new GenerateCommand(userId, request.workspaceId(), request.date(), eventId, request.meetingTitle()));
        return ResponseEntity.ok(GenerateResponse.from(result));
    }

    public record GenerateRequest(@NotNull UUID workspaceId, @NotNull LocalDate date, String meetingTitle) {
    }

    public record GenerateResponse(int tasksCreated, int checklistItemsCreated, boolean noteUpdated) {
        static GenerateResponse from(GenerateResult result) {
            return new GenerateResponse(result.tasksCreated(), result.checklistItemsCreated(), result.noteUpdated());
        }
    }
}
