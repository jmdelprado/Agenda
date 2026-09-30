package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase.DayNoteResult;
import com.agenda.kanban.note.application.port.in.SaveDayNoteUseCase;
import com.agenda.kanban.note.application.port.out.DayNoteRepositoryPort;
import com.agenda.kanban.note.domain.model.DayNote;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link SaveDayNoteUseCase}. Upsert: crea la nota del día si no existía, o actualiza su contenido. */
@Service
public class SaveDayNoteService implements SaveDayNoteUseCase {

    private final DayNoteRepositoryPort dayNoteRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public SaveDayNoteService(DayNoteRepositoryPort dayNoteRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.dayNoteRepository = dayNoteRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public DayNoteResult save(SaveDayNoteCommand command) {
        NoteAccess.requireOwnedWorkspace(command.userId(), command.workspaceId(), workspaceRepository);
        Instant now = Instant.now();
        DayNote existing = dayNoteRepository
                .findByWorkspaceIdAndDate(command.workspaceId(), command.date())
                .orElse(null);
        DayNote toSave = existing != null
                ? existing.withContent(command.content(), now)
                : DayNote.create(UUID.randomUUID(), command.workspaceId(), command.date(), command.content(), now);
        DayNote saved = dayNoteRepository.save(toSave);
        return new DayNoteResult(saved.getDate(), saved.getContent());
    }
}
