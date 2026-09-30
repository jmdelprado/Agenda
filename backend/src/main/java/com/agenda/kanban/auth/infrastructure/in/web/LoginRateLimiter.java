package com.agenda.kanban.auth.infrastructure.in.web;

import com.agenda.kanban.shared.TooManyRequestsException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Limitador de intentos de login en memoria, por email normalizado (T073, hardening de
 * seguridad). Ventana fija: máximo {@value #MAX_ATTEMPTS} intentos por {@link #WINDOW}; se
 * reinicia al primer intento tras expirar la ventana o inmediatamente tras un login correcto.
 *
 * <p>Es un componente de infraestructura (bean en memoria, sin persistencia) por diseño: evita
 * añadir infraestructura nueva (research.md §2 ya descartó dependencias externas para lo básico).
 * Limitación conocida: al no ser un almacén compartido, no protege un despliegue multi-instancia
 * sin sticky sessions — el primer paso al escalar horizontalmente sería mover este contador a
 * Redis o equivalente.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Window> attemptsByEmail = new ConcurrentHashMap<>();

    public void checkAllowed(String email) {
        Window window = attemptsByEmail.compute(normalize(email), (key, existing) -> {
            Instant now = Instant.now();
            if (existing == null || existing.startedAt().plus(WINDOW).isBefore(now)) {
                return new Window(now, 1);
            }
            return new Window(existing.startedAt(), existing.attempts() + 1);
        });
        if (window.attempts() > MAX_ATTEMPTS) {
            throw new TooManyRequestsException(
                    "Demasiados intentos de inicio de sesión. Inténtalo de nuevo en un minuto.");
        }
    }

    public void recordSuccess(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record Window(Instant startedAt, int attempts) {
    }
}
