package com.example.backend.service;

import com.example.backend.dto.CreateShortenerRequest;
import com.example.backend.dto.ShortenerResponse;
import com.example.backend.dto.UpdateShortenerRequest;
import com.example.backend.entity.Shortener;
import com.example.backend.entity.User;
import com.example.backend.repository.ShortenerRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    private static final int SHORT_CODE_LENGTH = 8;

    private final ShortenerRepository shortenerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ShortenerResponse> findAllByUserId(Long userId) {
        return shortenerRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(ShortenerResponse::fromEntity)
                .toList();
    }

    /** List all short links (QR codes) across all users. For admin only. */
    @Transactional(readOnly = true)
    public List<ShortenerResponse> findAllForAdmin() {
        return shortenerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ShortenerResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShortenerResponse findById(Long id, Long userId) {
        Shortener s = shortenerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Short link not found"));
        if (s.getUser() == null || !s.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Short link not found");
        }
        return ShortenerResponse.fromEntity(s);
    }

    @Transactional
    public ShortenerResponse create(CreateShortenerRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String shortCode = request.getShortCode() != null && !request.getShortCode().isBlank()
                ? request.getShortCode().trim().toLowerCase()
                : generateUniqueShortCode();

        if (shortenerRepository.existsByShortCode(shortCode)) {
            throw new IllegalArgumentException("Short code already in use: " + shortCode);
        }

        String fullUrl = request.getFullUrl().trim();
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            fullUrl = "https://" + fullUrl;
        }

        Shortener s = new Shortener();
        s.setShortCode(shortCode);
        s.setFullUrl(fullUrl);
        s.setUser(user);
        s.setExpiresAt(request.getExpiresAt());
        s.setActive(true);
        s = shortenerRepository.save(s);
        return ShortenerResponse.fromEntity(s);
    }

    @Transactional
    public ShortenerResponse update(Long id, UpdateShortenerRequest request, Long userId) {
        Shortener s = shortenerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Short link not found"));
        if (s.getUser() == null || !s.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Short link not found");
        }

        if (request.getFullUrl() != null && !request.getFullUrl().isBlank()) {
            String fullUrl = request.getFullUrl().trim();
            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                fullUrl = "https://" + fullUrl;
            }
            s.setFullUrl(fullUrl);
        }
        if (request.getExpiresAt() != null) {
            s.setExpiresAt(request.getExpiresAt());
        }
        if (request.getActive() != null) {
            s.setActive(request.getActive());
        }
        s = shortenerRepository.save(s);
        return ShortenerResponse.fromEntity(s);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Shortener s = shortenerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Short link not found"));
        if (s.getUser() == null || !s.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Short link not found");
        }
        shortenerRepository.delete(s);
    }

    /**
     * Resolve short code to full URL and increment click count. Public (no auth).
     */
    @Transactional
    public String resolveAndIncrementClick(String shortCode) {
        Shortener s = shortenerRepository.findByShortCode(shortCode.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Short link not found"));

        if (!s.isActive()) {
            throw new IllegalArgumentException("Short link is disabled");
        }
        if (s.getExpiresAt() != null && Instant.now().isAfter(s.getExpiresAt())) {
            throw new IllegalArgumentException("Short link has expired");
        }

        s.setClickCount(s.getClickCount() + 1);
        shortenerRepository.save(s);
        return s.getFullUrl();
    }

    private String generateUniqueShortCode() {
        for (int i = 0; i < 10; i++) {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, SHORT_CODE_LENGTH);
            if (!shortenerRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate unique short code");
    }
}
