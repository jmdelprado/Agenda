package com.agenda.kanban.note.application.service;

import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase;
import com.agenda.kanban.note.application.port.out.DayNoteRepositoryPort;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ver {@link GetDayNotesUseCase}. */
@Service
public class GetDayNotesService implements GetDayNotesUseCase {

    private final DayNoteRepositoryPort dayNoteRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    public GetDayNotesService(DayNoteRepositoryPort dayNoteRepository, WorkspaceRepositoryPort workspaceRepository) {
        this.dayNoteRepository = dayNoteRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DayNoteResult> getNotes(UUID userId, UUID workspaceId) {
        NoteAccess.requireOwnedWorkspace(userId, workspaceId, workspaceRepository);
        return dayNoteRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(note -> new DayNoteResult(note.getDate(), note.getContent()))
                .toList();
    }
}
