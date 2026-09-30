package com.agenda.kanban.auth.application.service;

import com.agenda.kanban.auth.application.port.in.RefreshTokenUseCase;
import com.agenda.kanban.auth.application.port.out.TokenGeneratorPort;
import com.agenda.kanban.auth.application.port.out.UserRepositoryPort;
import com.agenda.kanban.auth.domain.exception.InvalidCredentialsException;
import com.agenda.kanban.auth.domain.model.User;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenGeneratorPort tokenGenerator;
    private final UserRepositoryPort userRepository;

    public RefreshTokenService(TokenGeneratorPort tokenGenerator, UserRepositoryPort userRepository) {
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
    }

    @Override
    public RefreshedAccessToken refresh(RefreshTokenCommand command) {
        UUID userId = tokenGenerator.validateRefreshTokenAndGetUserId(command.refreshToken());
        User user = userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
        String accessToken = tokenGenerator.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenGenerator.generateRefreshToken(user.getId(), user.getEmail());
        return new RefreshedAccessToken(accessToken, refreshToken);
    }
}
