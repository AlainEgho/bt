package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDetailDto {

    private Long id;
    @NotNull(message = "Quantity is required")
    @Min(0)
    private Integer quantity = 1;
    @NotNull(message = "Price is required")
    @DecimalMin("0")
    private BigDecimal price = BigDecimal.ZERO;

    public static ItemDetailDto fromEntity(com.example.backend.entity.ItemDetail d) {
        if (d == null) return null;
        ItemDetailDto dto = new ItemDetailDto();
        dto.setId(d.getId());
        dto.setQuantity(d.getQuantity());
        dto.setPrice(d.getPrice());
        return dto;
    }
}
