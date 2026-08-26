package com.example.booking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void validLoginReturnsJwt() {
        ResponseEntity<Map> response = rest.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", "user", "password", "user123"),
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("token"));
        assertEquals("USER", response.getBody().get("role"));
    }

    @Test
    void invalidPasswordReturnsUnauthorized() {
        ResponseEntity<String> response = rest.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", "user", "password", "wrong"),
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
