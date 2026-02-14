package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateInvoiceRequest;
import com.example.backend.dto.InvoiceResponse;
import com.example.backend.dto.UpdateInvoiceRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        List<InvoiceResponse> invoices = invoiceService.findAllByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("OK", invoices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        InvoiceResponse invoice = invoiceService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", invoice));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        InvoiceResponse invoice = invoiceService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Invoice created", invoice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        InvoiceResponse invoice = invoiceService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Invoice updated", invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        invoiceService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted"));
    }
}
