package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateWallPostRequest;
import com.example.backend.dto.WallPostResponse;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.WallPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wall")
@RequiredArgsConstructor
public class WallController {

    private final WallPostService wallPostService;

    /** Publish a post to the wall (text and/or image). */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<WallPostResponse>> create(
            @Valid @RequestBody CreateWallPostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        WallPostResponse post = wallPostService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post published", post));
    }

    /** List wall posts with pagination. Newest first. */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<WallPostResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "100") int limit) {
        if (size > limit) size = limit;
        Page<WallPostResponse> posts = wallPostService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success("OK", posts));
    }
}
