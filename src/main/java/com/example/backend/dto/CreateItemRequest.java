package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Request to create an item. The item's user_id is set from the JWT (authenticated user), not from this DTO. */
@Data
public class CreateItemRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 500)
    private String description;

    @NotNull(message = "Category id is required")
    private String categoryId;

    private String imageBase64;
    private String imageContentType;
    private Boolean active;

    @Valid
    private ItemDetailDto detail;

    @Valid
    private AddressDto address;

    @Valid
    private ContactDto contact;
}
