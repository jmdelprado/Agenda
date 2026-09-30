package com.agenda.kanban.auth.infrastructure.in.web;

import com.agenda.kanban.auth.application.port.in.AuthenticateUserUseCase;
import com.agenda.kanban.auth.application.port.in.AuthenticateUserUseCase.AuthenticateUserCommand;
import com.agenda.kanban.auth.application.port.in.RefreshTokenUseCase;
import com.agenda.kanban.auth.application.port.in.RefreshTokenUseCase.RefreshTokenCommand;
import com.agenda.kanban.auth.application.port.in.RegisterUserUseCase;
import com.agenda.kanban.auth.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LoginRateLimiter loginRateLimiter) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UUID userId = registerUserUseCase.register(new RegisterUserCommand(request.email(), request.password()));
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId))
                .body(new RegisterResponse(userId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        loginRateLimiter.checkAllowed(request.email());
        var tokens = authenticateUserUseCase.login(new AuthenticateUserCommand(request.email(), request.password()));
        loginRateLimiter.recordSuccess(request.email());
        log.info("Login correcto");
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var result = refreshTokenUseCase.refresh(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(new RefreshResponse(result.accessToken(), result.refreshToken()));
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RegisterResponse(UUID userId) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginResponse(String accessToken, String refreshToken) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record RefreshResponse(String accessToken, String refreshToken) {
    }
}
