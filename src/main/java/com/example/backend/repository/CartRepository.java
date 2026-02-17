package com.example.backend.repository;

import com.example.backend.entity.Cart;
import com.example.backend.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    List<Cart> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Cart> findByIdAndUser_Id(String id, Long userId);

    List<Cart> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, CartStatus status);
}
