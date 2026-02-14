package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateShortenerRequest;
import com.example.backend.dto.ShortenerResponse;
import com.example.backend.dto.UpdateShortenerRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.ShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shorteners")
@RequiredArgsConstructor
public class ShortenerController {

    private final ShortenerService shortenerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShortenerResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ShortenerResponse> list = shortenerService.findAllByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShortenerResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ShortenerResponse s = shortenerService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", s));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShortenerResponse>> create(
            @Valid @RequestBody CreateShortenerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ShortenerResponse s = shortenerService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Short link created", s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShortenerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShortenerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ShortenerResponse s = shortenerService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Short link updated", s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        shortenerService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Short link deleted"));
    }
}
