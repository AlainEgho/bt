package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateRatingRequest;
import com.example.backend.dto.RatingResponse;
import com.example.backend.dto.UpdateRatingRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /** Public: get all ratings for an item. */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getByItemId(@PathVariable String itemId) {
        List<RatingResponse> ratings = ratingService.findByItemId(itemId);
        return ResponseEntity.ok(ApiResponse.success("OK", ratings));
    }

    /** Get my ratings. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getMyRatings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<RatingResponse> ratings = ratingService.findByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", ratings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RatingResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        RatingResponse rating = ratingService.findById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", rating));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Valid @RequestBody CreateRatingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RatingResponse rating = ratingService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rating created", rating));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRatingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RatingResponse rating = ratingService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Rating updated", rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ratingService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Rating deleted"));
    }
}
