package com.example.backend.dto;

import com.example.backend.entity.Cart;
import com.example.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class ItemBuyerDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private String cartId;
    private LocalDate cartEventDate;
    private Instant cartCreatedAt;

    public static ItemBuyerDto fromEntity(User user) {
        if (user == null) return null;
        return ItemBuyerDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .build();
    }

    /** From a cart that contains the seller's items: buyer (cart user) + cart dates. */
    public static ItemBuyerDto fromCart(Cart cart) {
        if (cart == null) return null;
        User user = cart.getUser();
        return ItemBuyerDto.builder()
                .id(user != null ? user.getId() : null)
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .country(user != null ? user.getCountry() : null)
                .cartId(cart.getId())
                .cartEventDate(cart.getEventDate())
                .cartCreatedAt(cart.getCreatedAt())
                .build();
    }
}
