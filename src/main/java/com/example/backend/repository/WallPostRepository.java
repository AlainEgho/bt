package com.example.backend.repository;

import com.example.backend.entity.WallPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface WallPostRepository extends JpaRepository<WallPost, Long> {

    Page<WallPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("DELETE FROM WallPost w WHERE w.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") Instant before);
}
