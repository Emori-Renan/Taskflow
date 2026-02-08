package com.taskflow.auth.infrastructure.adapter.out.jwt;

import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProviderAdapter implements TokenProviderPort {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey(String secretString) {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.email()) 
                .claim("role", user.role())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(secret))
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.email())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(refreshSecret))
                .compact();
    }

    @Override
    public String extractEmailFromRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(refreshSecret)) // Modern: verifyWith()
                .build()
                .parseSignedClaims(token) // Modern: parseSignedClaims()
                .getPayload() // Modern: getPayload()
                .getSubject();
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey(refreshSecret))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}