package com.example.backend.service;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.dto.UpdateCategoryRequest;
import com.example.backend.entity.Category;
import com.example.backend.entity.User;
import com.example.backend.repository.CategoryRepository;
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
public class CategoryService {

    private static final String DEFAULT_CONTENT_TYPE = "image/png";
    private static final String DATA_URL_PREFIX = "data:";
    private static final String BASE64_PREFIX = "base64,";

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.image-dir:uploads/images}")
    private String imageDir;

    @Value("${app.api.base-url:http://localhost:8081}")
    private String baseUrl;

    /** Public: list all active categories (no user filter). */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
                .map(c -> CategoryResponse.fromEntity(c, baseUrl.trim().replaceAll("/$", "")))
                .toList();
    }

    /** List categories for the current user (active only). */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllByUserId(Long userId) {
        return categoryRepository.findByUser_IdAndActiveTrueOrderByCreatedAtDesc(userId).stream()
                .map(c -> CategoryResponse.fromEntity(c, baseUrl.trim().replaceAll("/$", "")))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(String id, Long userId) {
        Category category = categoryRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return CategoryResponse.fromEntity(category, baseUrl.trim().replaceAll("/$", ""));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String categoryId = UUID.randomUUID().toString();
        ImageSaveResult imageResult = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            imageResult = saveCategoryImage(user.getId(), categoryId, request.getImageBase64(), request.getImageContentType());
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setDescription(request.getDescription().trim());
        category.setUser(user);
        category.setActive(request.getActive() != null ? request.getActive() : true);
        if (imageResult != null) {
            category.setImagePath(imageResult.relativePath());
            category.setImageContentType(imageResult.contentType());
        }
        category = categoryRepository.save(category);

        return CategoryResponse.fromEntity(category, baseUrl.trim().replaceAll("/$", ""));
    }

    @Transactional
    public CategoryResponse update(String id, UpdateCategoryRequest request, Long userId) throws IOException {
        Category category = categoryRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        category.setDescription(request.getDescription().trim());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            if (category.getImagePath() != null) {
                deleteCategoryImage(category);
            }
            ImageSaveResult result = saveCategoryImage(category.getUser().getId(), category.getId(), request.getImageBase64(), request.getImageContentType());
            categoryRepository.updateImagePathAndContentType(category.getId(), result.relativePath(), result.contentType());
            category.setImagePath(result.relativePath());
            category.setImageContentType(result.contentType());
        }

        return CategoryResponse.fromEntity(category, baseUrl.trim().replaceAll("/$", ""));
    }

    @Transactional
    public void delete(String id, Long userId) throws IOException {
        Category category = categoryRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (category.getImagePath() != null) {
            deleteCategoryImage(category);
        }
        categoryRepository.delete(category);
    }

    /** Admin: list all categories from all users. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllForAdmin() {
        return categoryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> CategoryResponse.fromEntity(c, baseUrl.trim().replaceAll("/$", "")))
                .toList();
    }

    /** For serving: get category by id (public). */
    @Transactional(readOnly = true)
    public Category findByIdPublic(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public Path getCategoryImagePath(Category category) {
        if (category.getImagePath() == null || category.getImagePath().isBlank()) {
            return null;
        }
        Path base = Path.of(imageDir);
        if (!base.isAbsolute()) {
            base = Path.of(System.getProperty("user.dir")).resolve(base);
        }
        return base.resolve("categories").resolve(category.getImagePath());
    }

    /**
     * Writes image to disk under categories/{userId}/{categoryId}. Does not touch the database.
     * @return relative path (e.g. "userId/categoryId") and content type for the caller to persist.
     */
    private ImageSaveResult saveCategoryImage(Long userId, String categoryId, String base64Data, String requestContentType) throws IOException {
        String contentType = resolveContentType(base64Data, requestContentType);
        if (base64Data.startsWith(DATA_URL_PREFIX)) {
            int idx = base64Data.indexOf(BASE64_PREFIX);
            if (idx >= 0) {
                base64Data = base64Data.substring(idx + BASE64_PREFIX.length());
            }
        }
        byte[] bytes = Base64.getDecoder().decode(base64Data.trim());
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Invalid or empty Base64 image data");
        }
        Path base = Path.of(imageDir);
        if (!base.isAbsolute()) {
            base = Path.of(System.getProperty("user.dir")).resolve(base);
        }
        Path categoryDir = base.resolve("categories").resolve(String.valueOf(userId));
        Files.createDirectories(categoryDir);
        String relativePath = userId + "/" + categoryId;
        Path filePath = categoryDir.resolve(categoryId);
        Files.write(filePath, bytes);
        return new ImageSaveResult(relativePath, contentType);
    }

    private record ImageSaveResult(String relativePath, String contentType) {}

    private void deleteCategoryImage(Category category) throws IOException {
        Path imagePath = getCategoryImagePath(category);
        if (imagePath != null && Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
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
}
