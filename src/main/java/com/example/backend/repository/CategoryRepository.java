package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Category> findByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    Optional<Category> findByIdAndUser_Id(String id, Long userId);

    List<Category> findAllByOrderByCreatedAtDesc();

    List<Category> findAllByActiveTrueOrderByCreatedAtDesc();

    /** Updates only image path and content type (avoids merge/optimistic locking). */
    @Modifying
    @Query("UPDATE Category c SET c.imagePath = :path, c.imageContentType = :contentType WHERE c.id = :id")
    int updateImagePathAndContentType(@Param("id") String id, @Param("path") String path, @Param("contentType") String contentType);
}
