package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ImageUploadResponse {

    private Long id;
    private String shortCode;
    /** Full URL to view the image, e.g. http://localhost:8081/i/abc12 */
    private String imageUrl;
    private String contentType;
    private String originalFileName;
    private Long userId;
    private Instant createdAt;

    public static ImageUploadResponse fromEntity(com.example.backend.entity.ImageUpload e, String baseUrl) {
        String url = baseUrl + "/i/" + e.getShortCode();
        return ImageUploadResponse.builder()
                .id(e.getId())
                .shortCode(e.getShortCode())
                .imageUrl(url)
                .contentType(e.getContentType())
                .originalFileName(e.getOriginalFileName())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
