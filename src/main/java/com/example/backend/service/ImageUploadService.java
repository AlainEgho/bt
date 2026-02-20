package com.example.backend.service;

import com.example.backend.dto.ImageUploadRequest;
import com.example.backend.dto.ImageUploadResponse;
import com.example.backend.entity.ImageUpload;
import com.example.backend.entity.User;
import com.example.backend.repository.ImageUploadRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private static final int SHORT_CODE_LENGTH = 8;
    private static final String DEFAULT_CONTENT_TYPE = "image/png";
    private static final String DATA_URL_PREFIX = "data:";
    private static final String BASE64_PREFIX = "base64,";

    private final ImageUploadRepository imageUploadRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.image-dir:uploads/images}")
    private String imageDir;

    @Value("${app.api.base-url:http://localhost:8081}")
    private String baseUrl;

    @Transactional
    public ImageUploadResponse upload(ImageUploadRequest request, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String base64Data = request.getBase64().trim();
        String contentType = resolveContentType(base64Data, request.getContentType());

        if (base64Data.startsWith(DATA_URL_PREFIX)) {
            int idx = base64Data.indexOf(BASE64_PREFIX);
            if (idx >= 0) {
                base64Data = base64Data.substring(idx + BASE64_PREFIX.length());
            }
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or empty Base64 image data", e);
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Invalid or empty Base64 image data");
        }

        String shortCode = generateUniqueShortCode();
        Path userDir = Path.of(imageDir).resolve(String.valueOf(userId));
        Files.createDirectories(userDir);
        String relativePath = userId + "/" + shortCode;
        Path filePath = userDir.resolve(shortCode);
        Files.write(filePath, bytes);

        ImageUpload entity = new ImageUpload();
        entity.setShortCode(shortCode);
        entity.setFilePath(relativePath);
        entity.setContentType(contentType);
        entity.setOriginalFileName(request.getOriginalFileName());
        entity.setUser(user);
        entity = imageUploadRepository.save(entity);

        return ImageUploadResponse.fromEntity(entity, baseUrl.trim().replaceAll("/$", ""));
    }

    @Transactional(readOnly = true)
    public List<ImageUploadResponse> findAllByUserId(Long userId) {
        return imageUploadRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(e -> ImageUploadResponse.fromEntity(e, baseUrl.trim().replaceAll("/$", "")))
                .toList();
    }

    @Transactional(readOnly = true)
    public ImageUploadResponse findById(Long id, Long userId) {
        ImageUpload e = imageUploadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image upload not found"));
        if (!e.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Image upload not found");
        }
        return ImageUploadResponse.fromEntity(e, baseUrl.trim().replaceAll("/$", ""));
    }

    @Transactional(readOnly = true)
    public List<ImageUploadResponse> findAllForAdmin() {
        return imageUploadRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(e -> ImageUploadResponse.fromEntity(e, baseUrl.trim().replaceAll("/$", "")))
                .toList();
    }

    /** For serving: resolve short code to file path and content type. */
    @Transactional(readOnly = true)
    public ImageUpload resolveByShortCode(String shortCode) {
        return imageUploadRepository.findByShortCode(shortCode.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }

    public Path getAbsoluteFilePath(ImageUpload upload) {
        return Path.of(imageDir).resolve(upload.getFilePath());
    }

    private String resolveContentType(String base64Data, String requestContentType) {
        if (requestContentType != null && !requestContentType.isBlank()) {
            return requestContentType.trim();
        }
        if (base64Data.startsWith(DATA_URL_PREFIX)) {
            int end = base64Data.indexOf(';');
            if (end > DATA_URL_PREFIX.length()) {
                return base64Data.substring(DATA_URL_PREFIX.length(), end).trim();
            }
        }
        return DEFAULT_CONTENT_TYPE;
    }

    private String generateUniqueShortCode() {
        for (int i = 0; i < 10; i++) {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, SHORT_CODE_LENGTH);
            if (!imageUploadRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate unique short code");
    }
}
