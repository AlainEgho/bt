package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateShortenerRequest {

    @NotBlank(message = "Full URL is required")
    @Size(max = 2048)
    private String fullUrl;

    /** Optional. If not provided, a random short code is generated. */
    @Size(min = 3, max = 32)
    private String shortCode;

    /** Optional expiry (UTC). */
    private Instant expiresAt;
}
