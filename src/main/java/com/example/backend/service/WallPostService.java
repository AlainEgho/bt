package com.example.backend.service;

import com.example.backend.dto.CreateWallPostRequest;
import com.example.backend.dto.ImageUploadRequest;
import com.example.backend.dto.ImageUploadResponse;
import com.example.backend.dto.WallPostResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.WallPost;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WallPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WallPostService {

    private final WallPostRepository wallPostRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    @Value("${app.wall.retention-days:30}")
    private int retentionDays = 30;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Transactional
    public WallPostResponse create(Long userId, CreateWallPostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String content = request.getContent() != null ? request.getContent().trim() : null;
        String imagePath;
        try {
            imagePath = resolveImagePath(userId, request);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save image: " + e.getMessage(), e);
        }

        if ((content == null || content.isBlank()) && (imagePath == null || imagePath.isBlank())) {
            throw new IllegalArgumentException("Post must have content and/or image (or imageBase64)");
        }

        WallPost post = new WallPost();
        post.setUser(user);
        post.setContent(content);
        post.setImagePath(imagePath);
        post = wallPostRepository.save(post);
        return WallPostResponse.fromEntity(post);
    }

    /** Resolve image path: upload base64 if provided, otherwise use imagePath. */
    private String resolveImagePath(Long userId, CreateWallPostRequest request) throws IOException {
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            ImageUploadRequest uploadRequest = new ImageUploadRequest();
            uploadRequest.setBase64(request.getImageBase64().trim());
            uploadRequest.setContentType(null);
            uploadRequest.setOriginalFileName(null);
            ImageUploadResponse response = imageUploadService.upload(uploadRequest, userId);
            return response.getImageUrl();
        }
        if (request.getImagePath() != null && !request.getImagePath().isBlank()) {
            return request.getImagePath().trim();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Page<WallPostResponse> findAll(int page, int size) {
        if (size <= 0) size = DEFAULT_PAGE_SIZE;
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return wallPostRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(WallPostResponse::fromEntity);
    }

    /** Delete posts older than retention period (default 1 month). */
    @Scheduled(fixedDelayString = "${app.wall.cleanup-interval-ms:86400000}") // default 24h
    @Transactional
    public void deleteOldPosts() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = wallPostRepository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Wall: removed {} post(s) older than {} days", deleted, retentionDays);
        }
    }
}
