package com.example.backend.dto;

import com.example.backend.entity.Bill.BillStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateBillRequest {

    private String billNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be greater than or equal to 0")
    private BigDecimal amount;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private BillStatus status;

    @Size(max = 1000)
    private String notes;
}
