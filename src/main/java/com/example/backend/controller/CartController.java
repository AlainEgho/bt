package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CartResponse;
import com.example.backend.dto.CreateCartRequest;
import com.example.backend.dto.ProcessPaymentRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.dto.UpdateCartRequest;
import com.example.backend.entity.CartStatus;
import com.example.backend.entity.Transaction;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CartService;
import com.example.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CartStatus status) {
        List<CartResponse> carts;
        if (status != null) {
            carts = cartService.findByUserIdAndStatus(principal.getId(), status);
        } else {
            carts = cartService.findAllByUserId(principal.getId());
        }
        return ResponseEntity.ok(ApiResponse.success("OK", carts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartResponse cart = cartService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", cart));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotal(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        java.math.BigDecimal total = cartService.getCartTotal(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", total));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> create(
            @Valid @RequestBody CreateCartRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartResponse cart = cartService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cart created", cart));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCartRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartResponse cart = cartService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        cartService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart deleted"));
    }

    /** Process payment for this cart (online or offline). Creates a transaction and updates cart status on success. */
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<TransactionResponse>> pay(
            @PathVariable String id,
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Transaction tx = paymentService.processPayment(id, principal.getId(), request.getPaymentMethod(), request.getExternalReference());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed", TransactionResponse.fromEntity(tx)));
    }

    /** List all transactions for this cart (payment history). */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TransactionResponse> list = paymentService.findTransactionsByCartId(id, principal.getId()).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }
}
