package com.example.backend.service;

import com.example.backend.dto.TransactionResponse;
import com.example.backend.entity.*;
import com.example.backend.payment.PaymentResult;
import com.example.backend.payment.PaymentStrategy;
import com.example.backend.payment.PaymentStrategyFactory;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ItemDetailRepository;
import com.example.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CartRepository cartRepository;
    private final TransactionRepository transactionRepository;
    private final ItemDetailRepository itemDetailRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    /**
     * Computes cart total from items' prices and quantities.
     */
    @Transactional(readOnly = true)
    public BigDecimal computeCartTotal(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getCartItems()) {
            if (ci.getItem() == null) continue;
            BigDecimal lineTotal = itemDetailRepository.findByItem_Id(ci.getItem().getId())
                    .map(d -> d.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity() != null ? ci.getQuantity() : 1)))
                    .orElse(BigDecimal.ZERO);
            total = total.add(lineTotal);
        }
        return total;
    }

    /**
     * Process payment for a cart: creates a transaction and updates cart status on success.
     */
    @Transactional
    public Transaction processPayment(String cartId, Long userId, PaymentMethod paymentMethod, String externalReference) {
        Cart cart = cartRepository.findByIdAndUser_Id(cartId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (cart.getStatus() == CartStatus.PAID || cart.getStatus() == CartStatus.COMPLETED) {
            throw new IllegalStateException("Cart already paid or completed");
        }

        BigDecimal amount = computeCartTotal(cart);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Cart total must be greater than zero");
        }

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentMethod)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment method: " + paymentMethod));

        PaymentResult result = strategy.process(amount, externalReference);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUser(cart.getUser());
        tx.setCart(cart);
        tx.setPaymentMethod(paymentMethod);
        tx.setAmount(amount);
        tx.setStatus(result.getStatus());
        tx.setExternalReference(result.getExternalReference());
        tx = transactionRepository.save(tx);

        if (result.isSuccess()) {
            cart.setStatus(CartStatus.PAID);
            cart.setPaymentMethod(paymentMethod);
            cartRepository.save(cart);
        }

        return tx;
    }

    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByCartId(String cartId, Long userId) {
        Cart cart = cartRepository.findByIdAndUser_Id(cartId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        return transactionRepository.findByCart_IdOrderByCreatedAtDesc(cart.getId());
    }

    /** Fast retrieval by user (uses user_id index, no cart join). */
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByUserId(Long userId) {
        return transactionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }
}
