package com.example.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateShortenerRequest {

    @Size(max = 2048)
    private String fullUrl;

    private Instant expiresAt;

    private Boolean active;
}
