package com.agenda.kanban.workspace.application.port.out;

import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Workspace}. Implementado en infrastructure/out/persistence. */
public interface WorkspaceRepositoryPort {

    Workspace save(Workspace workspace);

    Optional<Workspace> findById(UUID id);

    List<Workspace> findAllByUserId(UUID userId);

    /** Unicidad de {@code name} entre los espacios de trabajo NO archivados del usuario (data-model.md § Workspace). */
    boolean existsActiveByUserIdAndName(UUID userId, String name);
}
