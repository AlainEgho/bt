package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CategoryResponse {

    private String id;
    private String description;
    private String imageUrl;
    private boolean active;
    private Long userId;
    private Instant createdAt;
    private Instant updatedAt;

    public static CategoryResponse fromEntity(com.example.backend.entity.Category category, String baseUrl) {
        String imageUrl = null;
        if (category.getImagePath() != null && !category.getImagePath().isBlank()) {
            imageUrl = baseUrl.trim().replaceAll("/$", "") + "/api/categories/images/" + category.getImagePath();
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .description(category.getDescription())
                .imageUrl(imageUrl)
                .active(category.isActive())
                .userId(category.getUser() != null ? category.getUser().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
