package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CategoryResponse;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.dto.UpdateCategoryRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Public endpoint: list all categories (no authentication required).
     * Returns categories from all users.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list() {
        List<CategoryResponse> categories = categoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success("OK", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        CategoryResponse category = categoryService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            CategoryResponse category = categoryService.create(request, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Category created", category));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save image: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            CategoryResponse category = categoryService.update(id, request, principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Category updated", category));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            categoryService.delete(id, principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Category deleted"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }
}
