package com.example.backend.dto;

import com.example.backend.entity.CartStatus;
import com.example.backend.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateCartRequest {

    private CartStatus status = CartStatus.PENDING;

    private PaymentMethod paymentMethod;

    private LocalDate eventDate;

    @Valid
    @NotNull(message = "Cart items are required")
    private List<CartItemDto> items;
}
