package com.example.backend.repository;

import com.example.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByItem_IdOrderByCreatedAtDesc(String itemId);

    List<Rating> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Rating> findByIdAndUser_Id(Long id, Long userId);

    Optional<Rating> findByUser_IdAndItem_Id(Long userId, String itemId);
}
