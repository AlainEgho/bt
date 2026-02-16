package com.example.backend.controller;

import com.example.backend.entity.Item;
import com.example.backend.service.ItemService;
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

@RestController
@RequestMapping("/api/items/images")
@RequiredArgsConstructor
@Slf4j
public class ItemImageController {

    private final ItemService itemService;

    @GetMapping("/{userId}/{itemId}")
    public ResponseEntity<Resource> serveImageWithUser(@PathVariable Long userId, @PathVariable String itemId) {
        return serveImage(itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Resource> serveImage(@PathVariable String itemId) {
        Item item;
        try {
            item = itemService.findByIdPublic(itemId);
        } catch (IllegalArgumentException e) {
            log.debug("Item not found for image: {}", itemId);
            return ResponseEntity.notFound().build();
        }
        Path imagePath = itemService.getItemImagePath(item);
        if (imagePath == null) {
            log.debug("Item has no imagePath: {}", itemId);
            return ResponseEntity.notFound().build();
        }
        if (!imagePath.toFile().exists() || !imagePath.toFile().isFile()) {
            log.warn("Item image file not found at: {}", imagePath.toAbsolutePath());
            return ResponseEntity.notFound().build();
        }
        Resource resource = new PathResource(imagePath);
        if (!resource.isReadable()) {
            log.warn("Item image not readable: {}", imagePath.toAbsolutePath());
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(
                item.getImageContentType() != null ? item.getImageContentType() : "image/png");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }
}
