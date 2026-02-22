package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A post on the global wall. All users can publish and read.
 * Posts are deleted automatically after 1 month.
 */
@Entity
@Table(name = "wall_posts")
@Getter
@Setter
@NoArgsConstructor
public class WallPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10000)
    private String content;

    /** Optional image URL or path (e.g. from image upload API). */
    @Column(name = "image_path", length = 512)
    private String imagePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
