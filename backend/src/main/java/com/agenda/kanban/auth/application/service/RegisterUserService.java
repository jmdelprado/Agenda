package com.agenda.kanban.auth.application.service;

import com.agenda.kanban.auth.application.port.in.RegisterUserUseCase;
import com.agenda.kanban.auth.application.port.out.PasswordHasherPort;
import com.agenda.kanban.auth.application.port.out.UserRepositoryPort;
import com.agenda.kanban.auth.domain.exception.EmailAlreadyRegisteredException;
import com.agenda.kanban.auth.domain.model.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public RegisterUserService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UUID register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }
        User user = User.register(
                UUID.randomUUID(),
                command.email(),
                passwordHasher.hash(command.rawPassword()),
                Instant.now());
        return userRepository.save(user).getId();
    }
}
