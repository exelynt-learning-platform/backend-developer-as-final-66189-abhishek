package com.example.booking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

class SecurityIntegrationTest extends IntegrationTestBase {

    @Test
    void protectedResourceEndpointWithoutJwtIsUnauthorized() {
        ResponseEntity<String> response =
                rest.getForEntity(baseUrl() + "/resources", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void userCannotCreateResource() {
        String token = login("user", "user123");
        HttpEntity<?> request = new HttpEntity<>(
                java.util.Map.of("name", "Restricted", "description", "No"),
                authHeaders(token));

        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/resources", HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void adminCanCreateResource() {
        String token = login("admin", "admin123");
        HttpEntity<?> request = new HttpEntity<>(
                java.util.Map.of("name", "Admin Resource", "description", "Created by admin"),
                authHeaders(token));

        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/resources", HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void userCanReadResources() {
        String token = login("user", "user123");
        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/resources", HttpMethod.GET,
                authenticated(token), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
