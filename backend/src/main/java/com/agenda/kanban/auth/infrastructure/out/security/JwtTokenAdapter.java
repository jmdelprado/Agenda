package com.agenda.kanban.auth.infrastructure.out.security;

import com.agenda.kanban.auth.application.port.out.TokenGeneratorPort;
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

/** Implementa {@link TokenGeneratorPort} con JSON Web Tokens firmados con HMAC-SHA256 (ver research.md §2). */
@Component
public class JwtTokenAdapter implements TokenGeneratorPort {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtTokenAdapter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenExpirationMinutes);
        this.refreshTokenTtl = Duration.ofDays(refreshTokenExpirationDays);
    }

    @Override
    public String generateAccessToken(UUID userId, String email) {
        return buildToken(userId, email, TYPE_ACCESS, accessTokenTtl);
    }

    @Override
    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, TYPE_REFRESH, refreshTokenTtl);
    }

    @Override
    public UUID validateRefreshTokenAndGetUserId(String refreshToken) {
        return validateAndGetUserId(refreshToken, TYPE_REFRESH);
    }

    @Override
    public UUID validateAccessTokenAndGetUserId(String accessToken) {
        return validateAndGetUserId(accessToken, TYPE_ACCESS);
    }

    private UUID validateAndGetUserId(String token, String expectedType) {
        Claims claims = parseClaims(token);
        if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new UnauthorizedException("El tipo de token no es el esperado: " + expectedType);
        }
        return UUID.fromString(claims.getSubject());
    }

    private String buildToken(UUID userId, String email, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Token inválido o expirado");
        }
    }
}
