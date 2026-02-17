package com.example.backend.dto;

import com.example.backend.entity.CartStatus;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateCartRequest {

    private CartStatus status;

    private LocalDate eventDate;

    @Valid
    private List<CartItemDto> items;
}
