package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ShortenerResponse {

    private Long id;
    private String shortCode;
    private String fullUrl;
    private Long userId;
    private int clickCount;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean active;

    public static ShortenerResponse fromEntity(com.example.backend.entity.Shortener s) {
        return ShortenerResponse.builder()
                .id(s.getId())
                .shortCode(s.getShortCode())
                .fullUrl(s.getFullUrl())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .clickCount(s.getClickCount())
                .createdAt(s.getCreatedAt())
                .expiresAt(s.getExpiresAt())
                .active(s.isActive())
                .build();
    }
}
