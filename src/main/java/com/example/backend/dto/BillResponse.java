package com.example.backend.dto;

import com.example.backend.entity.Bill;
import com.example.backend.entity.Bill.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class BillResponse {

    private Long id;
    private String billNumber;
    private Long userId;
    private String title;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BillStatus status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static BillResponse fromEntity(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .userId(bill.getUser().getId())
                .title(bill.getTitle())
                .amount(bill.getAmount())
                .issueDate(bill.getIssueDate())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .notes(bill.getNotes())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
