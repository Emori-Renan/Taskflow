package com.taskflow.auth.infrastructure.adapter.out.jwt;

import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProviderAdapter implements TokenProviderPort {

    private static final String SECRET = "supersecretkeysupersecretkey";
    private static final long EXPIRATION = 3_600_000; // 1h

    @Override
    public String generateToken(User user) {
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.username())
                .claim("role", user.role())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
}
