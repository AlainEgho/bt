package com.example.backend.repository;

import com.example.backend.entity.ItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDetailRepository extends JpaRepository<ItemDetail, Long> {
}
