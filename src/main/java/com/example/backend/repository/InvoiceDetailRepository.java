package com.example.backend.repository;

import com.example.backend.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    List<InvoiceDetail> findByInvoice_IdOrderBySortOrderAscIdAsc(Long invoiceId);
}
