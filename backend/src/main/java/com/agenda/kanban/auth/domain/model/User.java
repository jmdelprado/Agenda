package com.agenda.kanban.auth.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Cuenta individual (FR-013: sin colaboración multi-usuario). POJO puro de dominio,
 * sin anotaciones de framework — ver plan.md (Arquitectura Hexagonal).
 */
public final class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;

    public User(UUID id, String email, String passwordHash, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt es obligatorio");
    }

    public static User register(UUID id, String email, String passwordHash, Instant now) {
        return new User(id, email, passwordHash, now);
    }

    private static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("El email es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("El email no tiene un formato válido: " + email);
        }
        return email;
    }

    private static String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("El hash de la contraseña es obligatorio");
        }
        return passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    /** Nunca se expone en respuestas de API; solo lo usa PasswordHasherPort para comparar. */
    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
