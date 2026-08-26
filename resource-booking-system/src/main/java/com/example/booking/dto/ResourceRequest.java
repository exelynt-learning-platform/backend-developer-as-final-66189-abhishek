package com.example.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        Boolean active
) {
}
