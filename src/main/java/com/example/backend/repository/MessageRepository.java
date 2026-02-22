package com.example.backend.repository;

import com.example.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Messages between two users (either direction), newest first. */
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userA AND m.receiver.id = :userB)
           OR (m.sender.id = :userB AND m.receiver.id = :userA)
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findConversation(@Param("userA") Long userA, @Param("userB") Long userB, Pageable pageable);

    /** Distinct peer user IDs that the current user has exchanged messages with (for conversation list). */
    @Query("""
        SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END
        FROM Message m
        WHERE m.sender.id = :userId OR m.receiver.id = :userId
        """)
    List<Long> findPeerUserIds(@Param("userId") Long userId);

    /** Count of unread messages sent to the given user by the given sender. */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.readAt IS NULL")
    long countUnreadFrom(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
