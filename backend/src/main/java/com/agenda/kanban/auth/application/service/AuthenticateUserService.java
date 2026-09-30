package com.agenda.kanban.auth.application.service;

import com.agenda.kanban.auth.application.port.in.AuthenticateUserUseCase;
import com.agenda.kanban.auth.application.port.out.PasswordHasherPort;
import com.agenda.kanban.auth.application.port.out.TokenGeneratorPort;
import com.agenda.kanban.auth.application.port.out.UserRepositoryPort;
import com.agenda.kanban.auth.domain.exception.InvalidCredentialsException;
import com.agenda.kanban.auth.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenGeneratorPort tokenGenerator;

    public AuthenticateUserService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public AuthTokens login(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenGenerator.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenGenerator.generateRefreshToken(user.getId(), user.getEmail());
        return new AuthTokens(accessToken, refreshToken);
    }
}
