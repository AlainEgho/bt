package com.example.backend.dto;

import com.example.backend.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessPaymentRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /** Optional: external reference (e.g. payment gateway id, cheque number). */
    private String externalReference;
}
