package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ImageUploadRequest;
import com.example.backend.dto.ImageUploadResponse;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * API for uploading images as Base64 and listing the current user's uploads.
 * Each upload gets a short code; the image is served at GET /i/{shortCode}.
 */
@RestController
@RequestMapping("/api/image-uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImageUploadResponse>> upload(
            @Valid @RequestBody ImageUploadRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ImageUploadResponse response = imageUploadService.upload(request, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Image uploaded. Use imageUrl or /i/{shortCode} to view.", response));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to store image: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> listMyUploads(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ImageUploadResponse> list = imageUploadService.findAllByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ImageUploadResponse response = imageUploadService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }
}
