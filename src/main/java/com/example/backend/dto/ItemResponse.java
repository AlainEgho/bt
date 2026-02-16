package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ItemResponse {

    private String id;
    private String description;
    private String imageUrl;
    private boolean active;
    private Long userId;
    private String categoryId;
    private ItemDetailDto detail;
    private AddressDto address;
    private ContactDto contact;
    private Instant createdAt;
    private Instant updatedAt;

    public static ItemResponse fromEntity(com.example.backend.entity.Item item, String baseUrl) {
        String imageUrl = null;
        if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            imageUrl = baseUrl.trim().replaceAll("/$", "") + "/api/items/images/" + item.getImagePath();
        }
        return ItemResponse.builder()
                .id(item.getId())
                .description(item.getDescription())
                .imageUrl(imageUrl)
                .active(item.isActive())
                .userId(item.getUser() != null ? item.getUser().getId() : null)
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .detail(ItemDetailDto.fromEntity(item.getDetail()))
                .address(AddressDto.fromEntity(item.getAddress()))
                .contact(ContactDto.fromEntity(item.getContact()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
