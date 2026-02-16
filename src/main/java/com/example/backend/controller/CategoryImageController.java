package com.example.backend.controller;

import com.example.backend.entity.Category;
import com.example.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

/**
 * Serves category images. Public endpoint (no auth).
 * Supports:
 * - GET /api/categories/images/{userId}/{categoryId} (imageUrl format from API)
 * - GET /api/categories/images/{categoryId} (by id only)
 */
@RestController
@RequestMapping("/api/categories/images")
@RequiredArgsConstructor
@Slf4j
public class CategoryImageController {

    private final CategoryService categoryService;

 
    /** Two segments: userId/categoryId (matches imageUrl from API). */
    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<Resource> serveImageWithUser(@PathVariable Long userId, @PathVariable String categoryId) {
        return serveImage(categoryId);
    }

    /** Single segment: categoryId only. */
    @GetMapping("/{categoryId}")
    public ResponseEntity<Resource> serveImage(@PathVariable String categoryId) {
        Category category;
        try {
            category = categoryService.findByIdPublic(categoryId);
        } catch (IllegalArgumentException e) {
            log.debug("Category not found for image: {}", categoryId);
            return ResponseEntity.notFound().build();
        }
        Path imagePath = categoryService.getCategoryImagePath(category);
        if (imagePath == null) {
            log.debug("Category has no imagePath: {}", categoryId);
            return ResponseEntity.notFound().build();
        }
        if (!imagePath.toFile().exists() || !imagePath.toFile().isFile()) {
            log.warn("Category image file not found at: {}", imagePath.toAbsolutePath());
            return ResponseEntity.notFound().build();
        }
        Resource resource = new PathResource(imagePath);
        if (!resource.isReadable()) {
            log.warn("Category image not readable: {}", imagePath.toAbsolutePath());
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(
                category.getImageContentType() != null ? category.getImageContentType() : "image/png");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }
}
