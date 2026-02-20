package com.example.backend.repository;

import com.example.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCart_IdOrderByCreatedAtDesc(String cartId);

    /** Fast retrieval by user without joining through cart. */
    List<Transaction> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
