package com.agenda.kanban.note.application.port.out;

import com.agenda.kanban.note.domain.model.DayNote;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link DayNote}. Implementado en infrastructure/out/persistence. */
public interface DayNoteRepositoryPort {

    DayNote save(DayNote note);

    Optional<DayNote> findByWorkspaceIdAndDate(UUID workspaceId, LocalDate date);

    List<DayNote> findAllByWorkspaceId(UUID workspaceId);
}
