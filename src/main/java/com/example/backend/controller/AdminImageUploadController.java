package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ImageUploadResponse;
import com.example.backend.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin API for listing all image uploads (all users).
 */
@RestController
@RequestMapping("/api/admin/image-uploads")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminImageUploadController {

    private final ImageUploadService imageUploadService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> listAll() {
        List<ImageUploadResponse> list = imageUploadService.findAllForAdmin();
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }
}
