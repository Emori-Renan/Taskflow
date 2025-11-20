package com.taskflow.auth.infrastructure.adapter.out.jwt;

import com.taskflow.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtProviderAdapterTest {

    private JwtProviderAdapter jwtProviderAdapter;
    private final String TEST_SECRET = "thisismysecretkeyforjwttokensthatisatleast256bitslong"; // 56 characters
    private final long TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtProviderAdapter = new JwtProviderAdapter();
        // Use ReflectionTestUtils to inject the @Value properties for the test
        ReflectionTestUtils.setField(jwtProviderAdapter, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtProviderAdapter, "expiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_shouldCreateValidTokenWithCorrectClaims() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User testUser = new User(userId, "testuser", "hashedpassword", "ROLE_TESTER");

        // Act
        String token = jwtProviderAdapter.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // Check basic JWT structure (header.payload.signature)

        // Deconstruct the token to verify claims (requires a token parser utility)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject(), "Subject should be the username.");
        assertEquals("ROLE_TESTER", claims.get("role"), "Claim 'role' should be correct.");
        
        // Check expiration time is in the future
        long expirationTime = claims.getExpiration().getTime();
        long issuedAtTime = claims.getIssuedAt().getTime();
        assertTrue(expirationTime > System.currentTimeMillis());
        // Check that expiration is roughly one hour after issuance (with a small buffer)
        assertTrue(expirationTime - issuedAtTime >= TEST_EXPIRATION - 1000);
    }

    @Test
    void generateToken_shouldThrowExceptionForInvalidSecret() {
        // Arrange (Secret is set, but we try to parse with a different one)
        User testUser = new User(UUID.randomUUID(), "testuser", "pass", "ROLE_USER");
        String token = jwtProviderAdapter.generateToken(testUser);
        String wrongSecret = "totallydifferentsecretkeythatisatleast256bitslong";

        // Assert
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            Jwts.parserBuilder()
                .setSigningKey(wrongSecret.getBytes())
                .build()
                .parseClaimsJws(token);
        }, "Token should fail validation with a different key.");
    }
}