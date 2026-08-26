package com.example.booking.service;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.entity.*;
import com.example.booking.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ResourceRepository resourceRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReservationService service;

    @Test
    void createUsesAuthenticatedUsernameInsteadOfRequestUserId() {
        User user = new User();
        user.setId(10L);
        user.setUsername("user");
        user.setRole(Role.USER);

        Resource resource = new Resource();
        resource.setId(20L);
        resource.setName("Room");
        resource.setActive(true);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(resource));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> {
                    Reservation r = inv.getArgument(0);
                    r.setId(99L);
                    return r;
                });

        ReservationRequest request = new ReservationRequest(
                20L, new BigDecimal("100.00"), ReservationStatus.PENDING,
                LocalDateTime.of(2026, 9, 1, 10, 0),
                LocalDateTime.of(2026, 9, 1, 11, 0));

        var response = service.create(request, "user");

        assertEquals(99L, response.id());
        assertEquals(10L, response.userId());
        assertEquals("user", response.username());
        verify(userRepository).findByUsername("user");
    }

    @Test
    void createRejectsEndBeforeStart() {
        ReservationRequest request = new ReservationRequest(
                20L, new BigDecimal("100.00"), ReservationStatus.PENDING,
                LocalDateTime.of(2026, 9, 1, 12, 0),
                LocalDateTime.of(2026, 9, 1, 11, 0));

        assertThrows(RuntimeException.class, () -> service.create(request, "user"));
        verifyNoInteractions(userRepository, resourceRepository, reservationRepository);
    }
}
