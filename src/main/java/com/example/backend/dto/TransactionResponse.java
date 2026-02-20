package com.example.backend.dto;

import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionResponse {

    private String id;
    private Long userId;
    private String cartId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private TransactionStatus status;
    private String externalReference;
    private Instant createdAt;

    public static TransactionResponse fromEntity(com.example.backend.entity.Transaction tx) {
        if (tx == null) return null;
        return TransactionResponse.builder()
                .id(tx.getId())
                .userId(tx.getUser() != null ? tx.getUser().getId() : null)
                .cartId(tx.getCart() != null ? tx.getCart().getId() : null)
                .paymentMethod(tx.getPaymentMethod())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .externalReference(tx.getExternalReference())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
