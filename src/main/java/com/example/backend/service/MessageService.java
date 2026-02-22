package com.example.backend.service;

import com.example.backend.dto.ConversationSummaryDto;
import com.example.backend.dto.MessageResponse;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int PREVIEW_LENGTH = 80;

    @Transactional
    public MessageResponse send(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }

        Message m = new Message();
        m.setSender(sender);
        m.setReceiver(receiver);
        m.setContent(content.trim());
        m = messageRepository.save(m);
        MessageResponse response = MessageResponse.fromEntity(m, senderId);
        // Notify receiver in real time via WebSocket (they subscribe to /user/queue/messages)
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", response);
        return response;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MessageResponse> getConversation(Long currentUserId, Long peerUserId, int page, int size) {
        if (size <= 0) size = DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findConversation(currentUserId, peerUserId, pageable)
                .map(msg -> MessageResponse.fromEntity(msg, currentUserId));
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryDto> getConversations(Long currentUserId) {
        List<Long> peerIds = messageRepository.findPeerUserIds(currentUserId);
        List<ConversationSummaryDto> result = new ArrayList<>();
        for (Long peerId : peerIds) {
            User peer = userRepository.findById(peerId).orElse(null);
            if (peer == null) continue;
            Pageable one = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
            Message last = messageRepository.findConversation(currentUserId, peerId, one)
                    .getContent().stream().findFirst().orElse(null);
            long unread = messageRepository.countUnreadFrom(currentUserId, peerId);
            result.add(ConversationSummaryDto.builder()
                    .peerUserId(peer.getId())
                    .peerFirstName(peer.getFirstName())
                    .peerLastName(peer.getLastName())
                    .peerEmail(peer.getEmail())
                    .lastMessagePreview(last != null ? truncate(last.getContent(), PREVIEW_LENGTH) : null)
                    .lastMessageAt(last != null ? last.getCreatedAt() : null)
                    .unreadCount((int) unread)
                    .build());
        }
        result.sort((a, b) -> {
            Instant at = a.getLastMessageAt();
            Instant bt = b.getLastMessageAt();
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.compareTo(at);
        });
        return result;
    }

    @Transactional
    public void markAsRead(Long messageId, Long readerUserId) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!m.getReceiver().getId().equals(readerUserId)) {
            throw new IllegalArgumentException("Message not found");
        }
        if (m.getReadAt() == null) {
            m.setReadAt(Instant.now());
            messageRepository.save(m);
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
