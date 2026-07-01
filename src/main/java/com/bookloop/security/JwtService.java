package com.bookloop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Issues and validates stateless JWTs. Access tokens are short-lived;
 * refresh tokens are long-lived and carry a "type=refresh" claim so an
 * access token can never be replayed against the /refresh endpoint.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-ms}") long accessTtlMs,
            @Value("${security.jwt.refresh-ttl-ms}") long refreshTtlMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public String generateAccessToken(String subject, String role) {
        return build(subject, Map.of("type", "access", "role", role), accessTtlMs);
    }

    public String generateRefreshToken(String subject) {
        return build(subject, Map.of("type", "refresh"), refreshTtlMs);
    }

    private String build(String subject, Map<String, ?> claims, long ttl) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(key)
                .compact();
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public boolean isValid(String token) {
        try {
            return parse(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parse(token));
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessTtlMs() {
        return accessTtlMs;
    }
}
