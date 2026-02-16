package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactDto {

    private Long id;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    private String phone;

    public static ContactDto fromEntity(com.example.backend.entity.Contact c) {
        if (c == null) return null;
        ContactDto dto = new ContactDto();
        dto.setId(c.getId());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setPhone(c.getPhone());
        return dto;
    }
}
