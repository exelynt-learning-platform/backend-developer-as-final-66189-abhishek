package com.example.booking.controller;

import com.example.booking.dto.*;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request, authentication.getName()));
    }

    @GetMapping
    public Page<ReservationResponse> search(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            Authentication authentication) {

        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        String[] parts = sort.split(",", 2);
        Sort.Direction direction = parts.length == 2
                ? Sort.Direction.fromOptionalString(parts[1]).orElse(Sort.Direction.DESC)
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, parts[0]));

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return service.search(authentication.getName(), admin, status, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable Long id, Authentication authentication) {
        return service.findById(id, authentication.getName(), isAdmin(authentication));
    }

    @PutMapping("/{id}")
    public ReservationResponse update(@PathVariable Long id,
                                      @Valid @RequestBody ReservationRequest request,
                                      Authentication authentication) {
        return service.update(id, request, authentication.getName(), isAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
