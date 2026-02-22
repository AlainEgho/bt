package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageResponse {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant createdAt;
    private Instant readAt;
    private boolean fromCurrentUser;

    public static MessageResponse fromEntity(com.example.backend.entity.Message m, Long currentUserId) {
        if (m == null) return null;
        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .receiverId(m.getReceiver() != null ? m.getReceiver().getId() : null)
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .readAt(m.getReadAt())
                .fromCurrentUser(m.getSender() != null && m.getSender().getId().equals(currentUserId))
                .build();
    }
}
