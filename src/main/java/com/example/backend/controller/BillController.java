package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.BillResponse;
import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.UpdateBillRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        List<BillResponse> bills = billService.findAllByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("OK", bills));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        BillResponse bill = billService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", bill));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> create(
            @Valid @RequestBody CreateBillRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BillResponse bill = billService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bill created", bill));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBillRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BillResponse bill = billService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Bill updated", bill));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        billService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Bill deleted"));
    }
}
