package com.example.backend.dto;

import com.example.backend.entity.CartStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CartResponse {

    private String id;
    private Long userId;
    private CartStatus status;
    private LocalDate eventDate;
    private List<CartItemDto> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static CartResponse fromEntity(com.example.backend.entity.Cart cart) {
        List<CartItemDto> items = cart.getCartItems() != null
                ? cart.getCartItems().stream().map(CartItemDto::fromEntity).toList()
                : List.of();
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .status(cart.getStatus())
                .eventDate(cart.getEventDate())
                .items(items)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
