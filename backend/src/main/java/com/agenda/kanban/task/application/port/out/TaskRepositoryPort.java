package com.agenda.kanban.task.application.port.out;

import com.agenda.kanban.task.domain.model.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link Task}. Implementado en infrastructure/out/persistence. */
public interface TaskRepositoryPort {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    /** Tareas de la columna ordenadas por fecha de creación ascendente. */
    List<Task> findByColumnIdOrderByCreatedAt(UUID columnId);

    void deleteById(UUID id);
}
