package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final PaymentService paymentService;

    /** List all transactions for the current user (fast retrieval by user_id). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TransactionResponse> list = paymentService.findTransactionsByUserId(principal.getId()).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }
}
