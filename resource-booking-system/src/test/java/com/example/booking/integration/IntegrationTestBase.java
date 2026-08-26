package com.example.booking.integration;

import com.example.booking.entity.*;
import com.example.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate rest;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ResourceRepository resourceRepository;
    @Autowired
    protected ReservationRepository reservationRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        Resource r1 = new Resource();
        r1.setName("Conference Room A");
        r1.setDescription("Test room");
        r1.setActive(true);
        resourceRepository.save(r1);

        Resource r2 = new Resource();
        r2.setName("Vehicle");
        r2.setDescription("Test vehicle");
        r2.setActive(true);
        resourceRepository.save(r2);
    }

    protected String login(String username, String password) {
        ResponseEntity<Map> response = rest.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", username, "password", password),
                Map.class);
        return (String) response.getBody().get("token");
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpEntity<?> authenticated(String token) {
        return new HttpEntity<>(authHeaders(token));
    }

    protected Map<String, Object> reservationBody(long resourceId, String price, String status,
                                                  String start, String end) {
        return Map.of(
                "resourceId", resourceId,
                "price", new BigDecimal(price),
                "status", status,
                "startTime", LocalDateTime.parse(start),
                "endTime", LocalDateTime.parse(end)
        );
    }
}
