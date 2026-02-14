package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.backend.entity.Role;
import com.example.backend.entity.UserType;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private boolean emailVerified;
    private Set<String> roles;

    public static AuthResponse fromUser(com.example.backend.entity.User user, String accessToken) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .map(n -> n.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames)
                .build();
    }
}
