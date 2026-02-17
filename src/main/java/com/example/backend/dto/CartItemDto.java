package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDto {

    private Long id;
    @NotNull(message = "Item id is required")
    private String itemId;
    @NotNull(message = "Quantity is required")
    @Min(1)
    private Integer quantity = 1;

    public static CartItemDto fromEntity(com.example.backend.entity.CartItem ci) {
        CartItemDto dto = new CartItemDto();
        dto.setId(ci.getId());
        dto.setItemId(ci.getItem() != null ? ci.getItem().getId() : null);
        dto.setQuantity(ci.getQuantity());
        return dto;
    }
}
