package com.taskflow.gateway.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // This must match the secret configured in the auth-service (application.yml)
    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars");

    private final Predicate<String> isPublicPath = path -> PUBLIC_PATHS.stream().anyMatch(path::startsWith);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPublicPath.test(path)) {
            return chain.filter(exchange);
        }

        // 1. Allow public access to all /api/auth paths (login/register) 
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or improperly formatted Authorization header for path: {}", path);
            return unauthorized(exchange);
        }

        // 3. Validate Token
        String token = authHeader.substring(7);
        try {
            // Ensure the secret is loaded using the same character set as the auth service
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            // Token is valid, continue the chain
            return chain.filter(exchange);
        } catch (JwtException e) {
            // Log the failure non-blockingly
            log.error("JWT Validation Failed for path {}: {}", path, e.getMessage());
            return unauthorized(exchange);
        }
    }

    /**
     * Helper method to set the response status to 401 Unauthorized.
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    /**
     * Filter should execute early (lower order value means higher priority).
     */
    @Override
    public int getOrder() {
        return -100; // Set a high priority to run before routing
    }
}