package com.agenda.kanban.workspace.domain.model;

import com.agenda.kanban.shared.ValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Espacio de trabajo / pestaña (FR-008, FR-009). POJO puro de dominio, sin anotaciones de
 * framework — ver plan.md (Arquitectura Hexagonal). Inmutable: las operaciones de negocio
 * devuelven una nueva instancia.
 */
public final class Workspace {

    private static final int NAME_MAX_LENGTH = 100;

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final Instant createdAt;
    private final Instant archivedAt;

    public Workspace(UUID id, UUID userId, String name, Instant createdAt, Instant archivedAt) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        this.name = validateName(name);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt es obligatorio");
        this.archivedAt = archivedAt;
    }

    public static Workspace create(UUID id, UUID userId, String name, Instant now) {
        return new Workspace(id, userId, name, now, null);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("El nombre del espacio de trabajo es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new ValidationException(
                    "El nombre del espacio de trabajo debe tener entre 1 y " + NAME_MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }

    public Workspace rename(String newName) {
        return new Workspace(id, userId, newName, createdAt, archivedAt);
    }

    public Workspace archive(Instant now) {
        return new Workspace(id, userId, name, createdAt, now);
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean belongsTo(UUID candidateUserId) {
        return userId.equals(candidateUserId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workspace workspace)) return false;
        return id.equals(workspace.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
