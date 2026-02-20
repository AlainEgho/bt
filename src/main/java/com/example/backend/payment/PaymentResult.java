package com.example.backend.payment;

import com.example.backend.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

/**
 * Result of a payment attempt.
 */
@Data
@Builder
public class PaymentResult {

    private boolean success;
    private TransactionStatus status;
    private String externalReference;
    private String message;
}
