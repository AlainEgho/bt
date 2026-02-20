package com.example.backend.payment;

import com.example.backend.entity.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Returns the appropriate payment strategy for a given payment method.
 */
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final List<PaymentStrategy> strategies;

    public Optional<PaymentStrategy> getStrategy(PaymentMethod method) {
        return strategies.stream()
                .filter(s -> s.getPaymentMethod() == method)
                .findFirst();
    }
}
