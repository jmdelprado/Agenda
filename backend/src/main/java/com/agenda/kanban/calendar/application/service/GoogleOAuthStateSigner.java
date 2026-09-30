package com.agenda.kanban.calendar.application.service;

import com.agenda.kanban.shared.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Firma/verifica el parámetro {@code state} del flujo OAuth de Google: un JWT de corta duración
 * (5 min) que lleva el userId, igual de firmado que los JWT de sesión (misma clave, {@code
 * app.jwt.secret}) pero sin tocar el módulo auth — es el único dato que sobrevive a la redirección
 * de ida y vuelta a Google para saber a qué usuario pertenece el callback.
 */
@Component
class GoogleOAuthStateSigner {

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String PURPOSE_VALUE = "google-oauth-state";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final SecretKey signingKey;

    GoogleOAuthStateSigner(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    String sign(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(PURPOSE_CLAIM, PURPOSE_VALUE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TTL)))
                .signWith(signingKey)
                .compact();
    }

    UUID verifyAndGetUserId(String state) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(state).getPayload();
            if (!PURPOSE_VALUE.equals(claims.get(PURPOSE_CLAIM, String.class))) {
                throw new UnauthorizedException("Estado OAuth inválido");
            }
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Estado OAuth inválido o expirado");
        }
    }
}
