package com.example.backend.dto;

import com.example.backend.entity.Invoice.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long userId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<InvoiceDetailDto> details;

    public static InvoiceResponse fromEntity(com.example.backend.entity.Invoice invoice) {
        List<InvoiceDetailDto> detailDtos = invoice.getDetails().stream()
                .map(d -> {
                    InvoiceDetailDto dto = new InvoiceDetailDto();
                    dto.setId(d.getId());
                    dto.setDescription(d.getDescription());
                    dto.setQuantity(d.getQuantity());
                    dto.setUnitPrice(d.getUnitPrice());
                    dto.setAmount(d.getAmount());
                    dto.setSortOrder(d.getSortOrder());
                    return dto;
                })
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .userId(invoice.getUser().getId())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .total(invoice.getTotal())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .details(detailDtos)
                .build();
    }
}
