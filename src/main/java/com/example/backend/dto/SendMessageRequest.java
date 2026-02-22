package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "Receiver user id is required")
    private Long receiverId;

    @NotBlank(message = "Message content is required")
    @Size(max = 10000)
    private String content;
}
