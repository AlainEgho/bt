package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** Optional. Base64 image data (raw or data URL format). If provided, replaces existing image. */
    private String imageBase64;

    /** Optional. Content type (e.g. image/png). Required if imageBase64 is not a data URL. */
    private String imageContentType;

    /** Optional. When false, category is inactive and excluded from list endpoints. */
    private Boolean active;
}
