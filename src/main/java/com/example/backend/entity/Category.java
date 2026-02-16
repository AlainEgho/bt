package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category entity with String id (stored as UUID string), description, and user association.
 * Extends BaseAuditEntity for createdAt/updatedAt audit fields.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseAuditEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 500)
    private String description;

    /** Relative path under the upload root, e.g. "categories/1/uuid" (no extension). */
    @Column(length = 512)
    private String imagePath;

    @Column(length = 500)
    private String imageContentType;

    /** When false, category is excluded from public and user list endpoints. */
    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
