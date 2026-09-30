package com.agenda.kanban.calendar.infrastructure.in.web;

import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase;
import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase.MeetingNoteResult;
import com.agenda.kanban.calendar.application.port.in.SaveMeetingNoteUseCase;
import com.agenda.kanban.calendar.application.port.in.SaveMeetingNoteUseCase.SaveMeetingNoteCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para las notas por reunión de Google Calendar (independientes de las notas del día). */
@RestController
@RequestMapping("/api/v1/calendar/google/notes")
public class MeetingNoteController {

    private final GetMeetingNotesUseCase getMeetingNotesUseCase;
    private final SaveMeetingNoteUseCase saveMeetingNoteUseCase;

    public MeetingNoteController(
            GetMeetingNotesUseCase getMeetingNotesUseCase, SaveMeetingNoteUseCase saveMeetingNoteUseCase) {
        this.getMeetingNotesUseCase = getMeetingNotesUseCase;
        this.saveMeetingNoteUseCase = saveMeetingNoteUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MeetingNoteResponse>> getNotes(@AuthenticationPrincipal UUID userId) {
        List<MeetingNoteResult> results = getMeetingNotesUseCase.getNotes(userId);
        return ResponseEntity.ok(results.stream().map(MeetingNoteResponse::from).toList());
    }

    /** PATCH en vez de PUT: la configuración CORS del backend (SecurityConfig) solo permite GET/POST/PATCH/DELETE. */
    @PatchMapping("/{eventId}")
    public ResponseEntity<MeetingNoteResponse> saveNote(
            @AuthenticationPrincipal UUID userId, @PathVariable String eventId, @Valid @RequestBody SaveNoteRequest request) {
        MeetingNoteResult result = saveMeetingNoteUseCase.save(new SaveMeetingNoteCommand(userId, eventId, request.content()));
        return ResponseEntity.ok(MeetingNoteResponse.from(result));
    }

    public record SaveNoteRequest(@Size(max = 5000) String content) {
    }

    public record MeetingNoteResponse(String eventId, String content) {
        static MeetingNoteResponse from(MeetingNoteResult result) {
            return new MeetingNoteResponse(result.eventId(), result.content());
        }
    }
}
