package com.example.backend.repository;

import com.example.backend.entity.Invoice;
import com.example.backend.entity.Invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByUser_IdAndStatus(Long userId, InvoiceStatus status);
}
