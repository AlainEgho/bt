package com.example.backend.payment;

import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Online payment strategy (e.g. card, gateway). For now simulates success;
 * integrate with a real payment gateway (Stripe, PayPal, etc.) as needed.
 */
@Component
@Slf4j
public class OnlinePaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ONLINE;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String externalReference) {
        log.debug("Processing online payment: amount={}, ref={}", amount, externalReference);
        // TODO: call payment gateway; for now simulate success
        return PaymentResult.builder()
                .success(true)
                .status(TransactionStatus.SUCCESS)
                .externalReference(externalReference != null ? externalReference : "online-" + System.currentTimeMillis())
                .message("Payment processed successfully")
                .build();
    }
}
