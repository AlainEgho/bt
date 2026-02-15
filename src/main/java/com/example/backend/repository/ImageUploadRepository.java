package com.example.backend.repository;

import com.example.backend.entity.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageUploadRepository extends JpaRepository<ImageUpload, Long> {

    Optional<ImageUpload> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<ImageUpload> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<ImageUpload> findAllByOrderByCreatedAtDesc();
}
