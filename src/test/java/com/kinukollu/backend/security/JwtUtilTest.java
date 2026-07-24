package com.kinukollu.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_shouldProduceNonNullToken() {
        String token = jwtUtil.generateToken("test@example.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_shouldReturnOriginalEmail() {
        String email = "kiran@example.com";
        String token = jwtUtil.generateToken(email);

        String extracted = jwtUtil.extractEmail(token);

        assertEquals(email, extracted);
    }

    @Test
    void isTokenValid_shouldReturnTrueForMatchingEmailAndFreshToken() {
        String email = "kiran@example.com";
        String token = jwtUtil.generateToken(email);

        assertTrue(jwtUtil.isTokenValid(token, email));
    }

    @Test
    void isTokenValid_shouldReturnFalseForMismatchedEmail() {
        String token = jwtUtil.generateToken("kiran@example.com");

        assertFalse(jwtUtil.isTokenValid(token, "someoneelse@example.com"));
    }

    @Test
    void extractEmail_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.extractEmail("not-a-real-token"));
    }
}
