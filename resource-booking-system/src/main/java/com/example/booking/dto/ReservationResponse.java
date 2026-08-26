package com.example.booking.dto;

import com.example.booking.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        String username,
        Long resourceId,
        String resourceName,
        BigDecimal price,
        ReservationStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
