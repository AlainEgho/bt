package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WallPostResponse {

    private Long id;
    private Long authorId;
    private String authorFirstName;
    private String authorLastName;
    private String authorEmail;
    private String content;
    private String imagePath;
    private Instant createdAt;

    public static WallPostResponse fromEntity(com.example.backend.entity.WallPost post) {
        if (post == null) return null;
        return WallPostResponse.builder()
                .id(post.getId())
                .authorId(post.getUser() != null ? post.getUser().getId() : null)
                .authorFirstName(post.getUser() != null ? post.getUser().getFirstName() : null)
                .authorLastName(post.getUser() != null ? post.getUser().getLastName() : null)
                .authorEmail(post.getUser() != null ? post.getUser().getEmail() : null)
                .content(post.getContent())
                .imagePath(post.getImagePath())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
