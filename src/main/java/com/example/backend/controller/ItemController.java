package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateItemRequest;
import com.example.backend.dto.ItemBuyerDto;
import com.example.backend.dto.ItemResponse;
import com.example.backend.dto.UpdateItemRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> list() {
        List<ItemResponse> items = itemService.findAll();
        return ResponseEntity.ok(ApiResponse.success("OK", items));
    }

    /** Public: get active items by category id. */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> listByCategory(@PathVariable String categoryId) {
        List<ItemResponse> items = itemService.findByCategoryId(categoryId);
        return ResponseEntity.ok(ApiResponse.success("OK", items));
    }

    /** Logged-in user (item owner): list users who have added his items to their carts. */
    @GetMapping("/buyers")
    public ResponseEntity<ApiResponse<List<ItemBuyerDto>>> listBuyersOfMyItems(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ItemBuyerDto> buyers = itemService.findBuyersOfMyItems(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", buyers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ItemResponse item = itemService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ItemResponse item = itemService.create(request, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Item created", item));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save image: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ItemResponse item = itemService.update(id, request, principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Item updated", item));
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
            itemService.delete(id, principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Item deleted"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }
}
