package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RatingResponse {

    private Long id;
    private Long userId;
    private String itemId;
    private Integer rating;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static RatingResponse fromEntity(com.example.backend.entity.Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUser() != null ? rating.getUser().getId() : null)
                .itemId(rating.getItem() != null ? rating.getItem().getId() : null)
                .rating(rating.getRating())
                .description(rating.getDescription())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
