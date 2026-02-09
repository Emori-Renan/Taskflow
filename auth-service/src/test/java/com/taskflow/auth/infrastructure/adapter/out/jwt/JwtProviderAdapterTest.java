package com.taskflow.auth.infrastructure.adapter.out.jwt;

import com.taskflow.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderAdapterTest {

    private JwtProviderAdapter jwtProviderAdapter;
    private final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "thisismysecretkeyforjwttokensthatisatleast256bitslong".getBytes());
    private final long TEST_EXPIRATION = 3600000; // 1 hour
    private final String TEST_REFRESH_SECRET = Base64.getEncoder().encodeToString(
            "thisismyrefreshsecretkeyforjwttokensthatisatleast256bits".getBytes());
    private final long TEST_REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtProviderAdapter = new JwtProviderAdapter();
        ReflectionTestUtils.setField(jwtProviderAdapter, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtProviderAdapter, "expiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtProviderAdapter, "refreshSecret", TEST_REFRESH_SECRET);
        ReflectionTestUtils.setField(jwtProviderAdapter, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void generateToken_shouldCreateValidTokenWithCorrectClaims() {
        UUID userId = UUID.randomUUID();
        User testUser = new User(userId, "testuser", "hashedpassword", "ROLE_TESTER");

        String token = jwtProviderAdapter.generateToken(testUser);

        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey(TEST_SECRET))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testuser", claims.getSubject());
        assertEquals("ROLE_TESTER", claims.get("role"));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void generateToken_shouldThrowExceptionForInvalidSecret() {
        User testUser = new User(UUID.randomUUID(), "testuser", "pass", "ROLE_USER");
        String token = jwtProviderAdapter.generateToken(testUser);
        String wrongSecret = Base64.getEncoder().encodeToString(
                "totallydifferentsecretkeythatisatleast256bitslong".getBytes());

        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            Jwts.parser()
                .verifyWith(getSigningKey(wrongSecret))
                .build()
                .parseSignedClaims(token);
        });
    }

    @Test
    void generateRefreshToken_shouldCreateValidRefreshToken() {
        User testUser = new User(UUID.randomUUID(), "testuser@example.com", "hashedpassword", "ROLE_USER");

        String refreshToken = jwtProviderAdapter.generateRefreshToken(testUser);

        assertNotNull(refreshToken);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey(TEST_REFRESH_SECRET))
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        assertEquals("testuser@example.com", claims.getSubject());
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void extractEmailFromRefreshToken_shouldReturnEmail() {
        User testUser = new User(UUID.randomUUID(), "testuser@example.com", "hashedpassword", "ROLE_USER");
        String refreshToken = jwtProviderAdapter.generateRefreshToken(testUser);

        String email = jwtProviderAdapter.extractEmailFromRefreshToken(refreshToken);

        assertEquals("testuser@example.com", email);
    }

    @Test
    void isRefreshTokenValid_shouldReturnTrueForValidToken() {
        User testUser = new User(UUID.randomUUID(), "testuser@example.com", "hashedpassword", "ROLE_USER");
        String refreshToken = jwtProviderAdapter.generateRefreshToken(testUser);

        assertTrue(jwtProviderAdapter.isRefreshTokenValid(refreshToken));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtProviderAdapter.isRefreshTokenValid("invalid.token.here"));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalseForAccessToken() {
        User testUser = new User(UUID.randomUUID(), "testuser@example.com", "hashedpassword", "ROLE_USER");
        String accessToken = jwtProviderAdapter.generateToken(testUser);

        // Access token is signed with TEST_SECRET, but isRefreshTokenValid uses TEST_REFRESH_SECRET
        assertFalse(jwtProviderAdapter.isRefreshTokenValid(accessToken));
    }
}
