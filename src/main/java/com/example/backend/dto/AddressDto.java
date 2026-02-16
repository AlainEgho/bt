package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressDto {

    private Long id;
    @NotNull(message = "Address name is required")
    private String addressName;
    @NotNull(message = "Longitude is required")
    private Double longitude;
    @NotNull(message = "Latitude is required")
    private Double latitude;

    public static AddressDto fromEntity(com.example.backend.entity.Address a) {
        if (a == null) return null;
        AddressDto dto = new AddressDto();
        dto.setId(a.getId());
        dto.setAddressName(a.getAddressName());
        dto.setLongitude(a.getLongitude());
        dto.setLatitude(a.getLatitude());
        return dto;
    }
}
