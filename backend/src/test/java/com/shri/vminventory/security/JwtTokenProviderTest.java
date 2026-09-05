package com.shri.vminventory.security;

import com.shri.vminventory.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "this-is-a-very-secure-test-key-32-chars-long!";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, 3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateToken("testuser", Role.ROLE_ADMIN, "test@example.com");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsername(token));
        assertEquals(Role.ROLE_ADMIN, jwtTokenProvider.getRole(token));
        assertEquals("test@example.com", jwtTokenProvider.getEmail(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }
}
