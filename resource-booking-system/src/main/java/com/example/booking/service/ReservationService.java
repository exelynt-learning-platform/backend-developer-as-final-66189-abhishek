package com.example.booking.service;

import com.example.booking.dto.*;
import com.example.booking.entity.*;
import com.example.booking.exception.*;
import com.example.booking.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, String username) {
        validateTimes(request);
        User user = getUser(username);
        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new NotFoundException("Resource not found: " + request.resourceId()));
        if (!resource.isActive()) throw new BadRequestException("Resource is not active");

        Reservation r = new Reservation();
        r.setUser(user);
        r.setResource(resource);
        r.setPrice(request.price());
        r.setStatus(request.status() == null ? ReservationStatus.PENDING : request.status());
        r.setStartTime(request.startTime());
        r.setEndTime(request.endTime());
        return toResponse(reservationRepository.save(r));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> search(String username, boolean admin,
                                            ReservationStatus status, BigDecimal minPrice,
                                            BigDecimal maxPrice, Pageable pageable) {
        Long userId = admin ? null : getUser(username).getId();
        return reservationRepository.search(userId, status, minPrice, maxPrice, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id, String username, boolean admin) {
        Reservation r = getReservation(id);
        checkOwnership(r, username, admin);
        return toResponse(r);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest request, String username, boolean admin) {
        validateTimes(request);
        Reservation r = getReservation(id);
        checkOwnership(r, username, admin);

        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new NotFoundException("Resource not found: " + request.resourceId()));

        r.setResource(resource);
        r.setPrice(request.price());
        if (request.status() != null) r.setStatus(request.status());
        r.setStartTime(request.startTime());
        r.setEndTime(request.endTime());
        return toResponse(reservationRepository.save(r));
    }

    @Transactional
    public void delete(Long id, String username, boolean admin) {
        Reservation r = getReservation(id);
        checkOwnership(r, username, admin);
        reservationRepository.delete(r);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
    }

    private void checkOwnership(Reservation r, String username, boolean admin) {
        if (!admin && !r.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("You can access only your own reservations");
        }
    }

    private void validateTimes(ReservationRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(), r.getUser().getId(), r.getUser().getUsername(),
                r.getResource().getId(), r.getResource().getName(),
                r.getPrice(), r.getStatus(), r.getStartTime(), r.getEndTime());
    }
}
