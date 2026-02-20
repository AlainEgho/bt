package com.example.backend.payment;

import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Offline payment strategy (e.g. cash, bank transfer). Records the intent;
 * actual settlement is assumed to happen outside the system.
 */
@Component
@Slf4j
public class OfflinePaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.OFFLINE;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String externalReference) {
        log.debug("Recording offline payment: amount={}, ref={}", amount, externalReference);
        return PaymentResult.builder()
                .success(true)
                .status(TransactionStatus.SUCCESS)
                .externalReference(externalReference != null ? externalReference : "offline-" + System.currentTimeMillis())
                .message("Offline payment recorded")
                .build();
    }
}
