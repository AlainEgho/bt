package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** Send a message to another user. */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        MessageResponse msg = messageService.send(principal.getId(), request.getReceiverId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message sent", msg));
    }

    /** Get conversation with a specific user (paginated). */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        Page<MessageResponse> messages = messageService.getConversation(principal.getId(), userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("OK", messages));
    }

    /** List all conversations (peers + last message preview and unread count). */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationSummaryDto>>> getConversations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ConversationSummaryDto> list = messageService.getConversations(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }

    /** Mark a message as read (receiver only). */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        messageService.markAsRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Message marked as read"));
    }
}
