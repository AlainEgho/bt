package com.example.backend.controller;

import com.example.backend.entity.ImageUpload;
import com.example.backend.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
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
 * Serves uploaded images by short code. Public endpoint (no auth).
 * GET /i/{code} returns the image file; use this URL in a QR code or short link.
 */
@RestController
@RequestMapping("/i")
@RequiredArgsConstructor
public class ImageServeController {

    private final ImageUploadService imageUploadService;

    @GetMapping("/{code}")
    public ResponseEntity<Resource> serveImage(@PathVariable String code) {
        ImageUpload upload = imageUploadService.resolveByShortCode(code);
        Path path = imageUploadService.getAbsoluteFilePath(upload);
        Resource resource = new PathResource(path);
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(upload.getContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }
}
