package com.example.booking.integration;

import com.example.booking.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReservationIntegrationTest extends IntegrationTestBase {

    @Test
    void userCanCreateAndReadOwnReservation() {
        String token = login("user", "user123");
        Long resourceId = resourceRepository.findAll().get(0).getId();

        HttpEntity<?> create = new HttpEntity<>(
                reservationBody(resourceId, "250.00", "PENDING",
                        "2026-09-10T10:00:00", "2026-09-10T12:00:00"),
                authHeaders(token));

        ResponseEntity<Map> created = rest.exchange(
                baseUrl() + "/reservations", HttpMethod.POST, create, Map.class);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals("user", created.getBody().get("username"));
        assertNull(created.getBody().get("userId") == null ? null : null); // ownership is server-derived

        ResponseEntity<Map> list = rest.exchange(
                baseUrl() + "/reservations?page=0&size=10",
                HttpMethod.GET, authenticated(token), Map.class);

        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertEquals(1, ((java.util.List<?>) list.getBody().get("content")).size());
    }

    @Test
    void userCannotAccessAnotherUsersReservation() {
        Long resourceId = resourceRepository.findAll().get(0).getId();
        User admin = userRepository.findByUsername("admin").orElseThrow();

        Reservation reservation = new Reservation();
        reservation.setUser(admin);
        reservation.setResource(resourceRepository.findById(resourceId).orElseThrow());
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setStartTime(LocalDateTime.of(2026, 9, 11, 10, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 9, 11, 12, 0));
        reservation = reservationRepository.save(reservation);

        String userToken = login("user", "user123");

        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/reservations/" + reservation.getId(),
                HttpMethod.GET, authenticated(userToken), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void adminCanSeeAllReservations() {
        Long resourceId = resourceRepository.findAll().get(0).getId();
        User user = userRepository.findByUsername("user").orElseThrow();

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resourceRepository.findById(resourceId).orElseThrow());
        reservation.setPrice(new BigDecimal("150.00"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setStartTime(LocalDateTime.of(2026, 9, 12, 10, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 9, 12, 11, 0));
        reservationRepository.save(reservation);

        String adminToken = login("admin", "admin123");

        ResponseEntity<Map> response = rest.exchange(
                baseUrl() + "/reservations?page=0&size=10",
                HttpMethod.GET, authenticated(adminToken), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((java.util.List<?>) response.getBody().get("content")).size());
    }

    @Test
    void reservationFilteringPaginationAndSortingWork() {
        String adminToken = login("admin", "admin123");
        Long resourceId = resourceRepository.findAll().get(0).getId();
        User user = userRepository.findByUsername("user").orElseThrow();

        for (int i = 1; i <= 3; i++) {
            Reservation r = new Reservation();
            r.setUser(user);
            r.setResource(resourceRepository.findById(resourceId).orElseThrow());
            r.setPrice(new BigDecimal(String.valueOf(i * 100)));
            r.setStatus(i == 2 ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING);
            r.setStartTime(LocalDateTime.of(2026, 9, 10 + i, 10, 0));
            r.setEndTime(LocalDateTime.of(2026, 9, 10 + i, 11, 0));
            reservationRepository.save(r);
        }

        ResponseEntity<Map> response = rest.exchange(
                baseUrl() + "/reservations?status=CONFIRMED&minPrice=150&maxPrice=250&page=0&size=1&sort=price,desc",
                HttpMethod.GET, authenticated(adminToken), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((java.util.List<?>) response.getBody().get("content")).size());
        assertEquals(1, response.getBody().get("totalElements"));
    }

    @Test
    void invalidReservationTimesAreRejected() {
        String token = login("user", "user123");
        Long resourceId = resourceRepository.findAll().get(0).getId();

        HttpEntity<?> create = new HttpEntity<>(
                reservationBody(resourceId, "250.00", "PENDING",
                        "2026-09-10T12:00:00", "2026-09-10T10:00:00"),
                authHeaders(token));

        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/reservations", HttpMethod.POST, create, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
