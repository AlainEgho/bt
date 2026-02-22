package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConversationSummaryDto {

    private Long peerUserId;
    private String peerFirstName;
    private String peerLastName;
    private String peerEmail;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private int unreadCount;
}
