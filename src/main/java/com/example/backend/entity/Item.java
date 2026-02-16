package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item entity: description, image, active; belongs to a category and user.
 * Can have optional address and contact. Same pattern as Category.
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item extends BaseAuditEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "image_path", length = 512)
    private String imagePath;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private ItemDetail detail;
}
