package com.example.backend.controller;

import com.example.backend.service.ShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Public redirect endpoint. No authentication required.
 * GET /s/{code} redirects to the stored full URL and increments the click count.
 */
@RestController
@RequestMapping("/s")
@RequiredArgsConstructor
public class ShortenerRedirectController {

    private final ShortenerService shortenerService;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String fullUrl = shortenerService.resolveAndIncrementClick(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(fullUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
