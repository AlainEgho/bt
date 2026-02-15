package com.example.backend.repository;

import com.example.backend.entity.Shortener;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortenerRepository extends JpaRepository<Shortener, Long> {

    Optional<Shortener> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<Shortener> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Shortener> findAllByOrderByCreatedAtDesc();
}
