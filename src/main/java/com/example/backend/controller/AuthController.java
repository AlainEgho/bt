package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignUpRequest;
import com.example.backend.dto.ValidateTokenResponse;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please check your email to verify your account.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmailGet(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    /**
     * Validates a JWT. For use by other microservices: send the token in the Authorization header.
     * Returns valid=true with userId and email when the token is valid; valid=false otherwise.
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<ValidateTokenResponse>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.ok(ApiResponse.success("Token validation result",
                    ValidateTokenResponse.builder().valid(false).build()));
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Token validation result",
                    ValidateTokenResponse.builder().valid(false).build()));
        }
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok(ApiResponse.success("Token validation result",
                    ValidateTokenResponse.builder().valid(false).build()));
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String email = jwtTokenProvider.getEmailFromToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token validation result",
                ValidateTokenResponse.builder()
                        .valid(true)
                        .userId(userId)
                        .email(email)
                        .build()));
    }
}
