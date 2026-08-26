package com.example.booking.dto;

import com.example.booking.entity.ReservationStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull(message = "Resource ID is required") Long resourceId,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,
        ReservationStatus status,
        @NotNull(message = "Start time is required") LocalDateTime startTime,
        @NotNull(message = "End time is required") LocalDateTime endTime
) {
}
