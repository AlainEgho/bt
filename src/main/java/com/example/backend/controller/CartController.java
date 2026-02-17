package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CartResponse;
import com.example.backend.dto.CreateCartRequest;
import com.example.backend.dto.UpdateCartRequest;
import com.example.backend.entity.CartStatus;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CartService;
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
}
