package com.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRatingRequest {

    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
