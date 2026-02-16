package com.taskflow.gateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    // 256-bit key, Base64-encoded (matches auth-service's key derivation)
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "test-secret-key-that-is-long-enough-for-hmac-sha256".getBytes());

    private JwtAuthFilter jwtAuthFilter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() throws Exception {
        jwtAuthFilter = new JwtAuthFilter();

        Field secretField = JwtAuthFilter.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtAuthFilter, TEST_SECRET);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String generateValidToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("POST /api/auth/register should be permitted without JWT")
    void registerEndpoint_shouldBeAccessibleWithoutJwt() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/login should be permitted without JWT")
    void loginEndpoint_shouldBeAccessibleWithoutJwt() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET /api/auth/oauth2/google should be permitted without JWT")
    void oauthEndpoint_shouldBeAccessibleWithoutJwt() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/oauth2/google")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected endpoint without JWT should return 401")
    void protectedEndpoint_withoutJwt_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected endpoint with invalid JWT should return 401")
    void protectedEndpoint_withInvalidJwt_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected endpoint with malformed Authorization header should return 401")
    void protectedEndpoint_withMalformedAuthHeader_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Protected endpoint with valid JWT should pass and forward claims")
    void protectedEndpoint_withValidJwt_shouldPassAndForwardClaims() {
        String token = generateValidToken("user@test.com", "USER");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
