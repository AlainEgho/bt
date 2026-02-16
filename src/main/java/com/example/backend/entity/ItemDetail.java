package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Details for an item: quantity and price. One-to-one with Item.
 */
@Entity
@Table(name = "item_details")
@Getter
@Setter
@NoArgsConstructor
public class ItemDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true, updatable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price = BigDecimal.ZERO;
}
