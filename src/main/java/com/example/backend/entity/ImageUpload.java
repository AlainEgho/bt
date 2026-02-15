package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Stores metadata for an image uploaded as Base64 and saved on the server.
 * The image is accessible via a short link /i/{shortCode} (QR code / shortener style).
 */
@Entity
@Table(name = "image_uploads")
@Getter
@Setter
@NoArgsConstructor
public class ImageUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String shortCode;

    /** Relative path under the upload root, e.g. "1/abc12" (no extension). */
    @Column(nullable = false, length = 512)
    private String filePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(length = 255)
    private String originalFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
