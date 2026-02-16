package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ItemResponse;
import com.example.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> listAll() {
        List<ItemResponse> list = itemService.findAllForAdmin();
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }
}
