package com.example.booking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourceIntegrationTest extends IntegrationTestBase {

    @Test
    void adminCanUpdateAndDeleteResource() {
        String token = login("admin", "admin123");
        Long id = resourceRepository.findAll().get(0).getId();

        HttpEntity<?> update = new HttpEntity<>(
                Map.of("name", "Updated Room", "description", "Updated"),
                authHeaders(token));

        ResponseEntity<String> updated = rest.exchange(
                baseUrl() + "/resources/" + id, HttpMethod.PUT, update, String.class);

        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertTrue(updated.getBody().contains("Updated Room"));

        ResponseEntity<Void> deleted = rest.exchange(
                baseUrl() + "/resources/" + id, HttpMethod.DELETE,
                authenticated(token), Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());
        assertFalse(resourceRepository.existsById(id));
    }
}
