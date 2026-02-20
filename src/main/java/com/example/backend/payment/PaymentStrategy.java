package com.example.backend.payment;

import com.example.backend.entity.PaymentMethod;

import java.math.BigDecimal;

/**
 * Strategy for processing a payment (online or offline).
 * Implementations perform the actual payment flow and return the result.
 */
public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    /**
     * Process the payment. Implementations may call external gateways (online) or record only (offline).
     *
     * @param amount              total amount to charge
     * @param externalReference   optional reference (e.g. card token, cheque number)
     * @return result with success/failure and optional reference
     */
    PaymentResult process(BigDecimal amount, String externalReference);
}
