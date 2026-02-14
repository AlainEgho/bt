package com.example.backend.dto;

import com.example.backend.entity.Invoice.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateInvoiceRequest {

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private InvoiceStatus status;

    private BigDecimal taxAmount;

    private String notes;

    @Valid
    private List<InvoiceDetailDto> details; // if provided, replaces existing details
}
