package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rating entity: user can rate and comment on an item.
 * Rating is typically 1-5 stars, with optional description/comment.
 */
@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
public class Rating extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer rating; // Typically 1-5

    @Column(length = 1000)
    private String description; // Comment/description
}
