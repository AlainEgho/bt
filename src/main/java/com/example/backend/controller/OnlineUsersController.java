package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.OnlineUserDto;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.UserSignInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lists users currently signed in (activity within the last hour).
 * Excludes the current user so they can start messaging with others.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class OnlineUsersController {

    private final UserSignInService userSignInService;

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<OnlineUserDto>>> listOnline(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<OnlineUserDto> list = userSignInService.listOnlineUsers(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }
}
