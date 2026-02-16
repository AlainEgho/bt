package com.example.backend.repository;

import com.example.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String> {

    List<Item> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Item> findByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    Optional<Item> findByIdAndUser_Id(String id, Long userId);

    List<Item> findAllByOrderByCreatedAtDesc();

    List<Item> findAllByActiveTrueOrderByCreatedAtDesc();

    List<Item> findByCategory_IdAndActiveTrueOrderByCreatedAtDesc(String categoryId);

    @Modifying
    @Query("UPDATE Item i SET i.imagePath = :path, i.imageContentType = :contentType WHERE i.id = :id")
    int updateImagePathAndContentType(@Param("id") String id, @Param("path") String path, @Param("contentType") String contentType);
}
