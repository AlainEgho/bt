package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlineUserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String country;

    public static OnlineUserDto fromUser(com.example.backend.entity.User user) {
        if (user == null) return null;
        return OnlineUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .build();
    }
}
