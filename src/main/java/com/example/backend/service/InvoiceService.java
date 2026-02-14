package com.example.backend.service;

import com.example.backend.dto.CreateInvoiceRequest;
import com.example.backend.dto.InvoiceResponse;
import com.example.backend.dto.UpdateInvoiceRequest;
import com.example.backend.entity.Invoice;
import com.example.backend.entity.InvoiceDetail;
import com.example.backend.entity.Invoice.InvoiceStatus;
import com.example.backend.entity.User;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    private static final String INVOICE_NUMBER_PREFIX = "INV-";
    private static final DateTimeFormatter NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAllByUserId(Long userId) {
        return invoiceRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(InvoiceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findById(Long id, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        if (!invoice.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Invoice not found");
        }
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String invoiceNumber = request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()
                ? request.getInvoiceNumber().trim()
                : generateInvoiceNumber(userId);

        if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            throw new IllegalArgumentException("Invoice number already exists: " + invoiceNumber);
        }

        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);

        if (request.getDetails() != null) {
            int order = 0;
            for (var dto : request.getDetails()) {
                InvoiceDetail detail = mapDetailToEntity(dto, invoice, order++);
                invoice.getDetails().add(detail);
            }
        }

        recalculateTotals(invoice);
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional
    public InvoiceResponse update(Long id, UpdateInvoiceRequest request, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        if (!invoice.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Invoice not found");
        }

        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }
        if (request.getTaxAmount() != null) {
            invoice.setTaxAmount(request.getTaxAmount());
        }

        if (request.getDetails() != null) {
            invoice.getDetails().clear();
            int order = 0;
            for (var dto : request.getDetails()) {
                InvoiceDetail detail = mapDetailToEntity(dto, invoice, order++);
                invoice.getDetails().add(detail);
            }
        }

        recalculateTotals(invoice);
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        if (!invoice.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Invoice not found");
        }
        invoiceRepository.delete(invoice);
    }

    private InvoiceDetail mapDetailToEntity(com.example.backend.dto.InvoiceDetailDto dto, Invoice invoice, int sortOrder) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setInvoice(invoice);
        detail.setDescription(dto.getDescription().trim());
        detail.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : BigDecimal.ONE);
        detail.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);
        detail.setSortOrder(dto.getSortOrder());
        detail.recalculateAmount();
        return detail;
    }

    private void recalculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getDetails().stream()
                .map(InvoiceDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO);
        invoice.setTotal(subtotal.add(invoice.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
    }

    private String generateInvoiceNumber(Long userId) {
        String datePart = LocalDate.now().format(NUMBER_DATE_FORMAT);
        long count = invoiceRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().count();
        return INVOICE_NUMBER_PREFIX + datePart + "-" + (count + 1);
    }
}
