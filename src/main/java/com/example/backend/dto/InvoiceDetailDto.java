package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceDetailDto {

    private Long id;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    private BigDecimal amount; // computed by service

    private int sortOrder;
}
