package com.agenda.kanban.note.application.port.out;

import com.agenda.kanban.note.domain.model.ChecklistItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link ChecklistItem}. Implementado en infrastructure/out/persistence. */
public interface ChecklistItemRepositoryPort {

    ChecklistItem save(ChecklistItem item);

    Optional<ChecklistItem> findById(UUID id);

    List<ChecklistItem> findAllByWorkspaceId(UUID workspaceId);

    int countByWorkspaceIdAndDate(UUID workspaceId, LocalDate date);

    void deleteById(UUID id);
}
