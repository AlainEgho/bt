package com.example.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWallPostRequest {

    /** Optional text content. Can be empty if only image is posted. */
    @Size(max = 10000)
    private String content;

    /** Optional image URL or path (e.g. from POST /api/image-uploads). Ignored if imageBase64 is set. */
    @Size(max = 512)
    private String imagePath;

    /** Optional image as base64 (raw or data URL e.g. data:image/png;base64,...). Uploaded and saved; result used as image path. */
    private String imageBase64;
}
