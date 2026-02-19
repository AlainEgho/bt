package com.example.backend.repository;

import com.example.backend.entity.Bill;
import com.example.backend.entity.Bill.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Bill> findByBillNumber(String billNumber);

    boolean existsByBillNumber(String billNumber);

    List<Bill> findByUser_IdAndStatus(Long userId, BillStatus status);
}
