package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request to upload an image as Base64.
 * base64: either raw Base64 string or data URL (data:image/png;base64,...).
 * contentType: optional if base64 is a data URL; otherwise e.g. image/png, image/jpeg.
 */
@Data
public class ImageUploadRequest {

    @NotBlank(message = "Base64 image data is required")
    private String base64;

    /** e.g. image/png, image/jpeg. Optional when base64 is a data URL. */
    private String contentType;

    private String originalFileName;
}
