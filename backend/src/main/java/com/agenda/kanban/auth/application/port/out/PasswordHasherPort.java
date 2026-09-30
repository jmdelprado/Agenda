package com.agenda.kanban.auth.application.port.out;

/** Puerto de salida: hashing/verificación de contraseñas. Implementado con BCrypt en infrastructure/out/security. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
