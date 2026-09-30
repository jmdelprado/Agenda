package com.agenda.kanban.auth.application.port.out;

import com.agenda.kanban.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de {@link User}. Implementado por un adaptador en infrastructure/out/persistence. */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
