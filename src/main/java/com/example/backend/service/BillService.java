package com.example.backend.service;

import com.example.backend.dto.BillResponse;
import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.UpdateBillRequest;
import com.example.backend.entity.Bill;
import com.example.backend.entity.User;
import com.example.backend.repository.BillRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private static final String BILL_NUMBER_PREFIX = "BILL-";
    private static final DateTimeFormatter NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BillRepository billRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BillResponse> findAllByUserId(Long userId) {
        return billRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(BillResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillResponse findById(Long id, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));
        if (!bill.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bill not found");
        }
        return BillResponse.fromEntity(bill);
    }

    @Transactional
    public BillResponse create(CreateBillRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String billNumber = request.getBillNumber() != null && !request.getBillNumber().isBlank()
                ? request.getBillNumber().trim()
                : generateBillNumber(userId);

        if (billRepository.existsByBillNumber(billNumber)) {
            throw new IllegalArgumentException("Bill number already exists: " + billNumber);
        }

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setBillNumber(billNumber);
        bill.setTitle(request.getTitle().trim());
        bill.setAmount(request.getAmount());
        bill.setIssueDate(request.getIssueDate());
        bill.setDueDate(request.getDueDate());
        bill.setStatus(request.getStatus() != null ? request.getStatus() : Bill.BillStatus.PENDING);
        bill.setNotes(request.getNotes());

        bill = billRepository.save(bill);
        return BillResponse.fromEntity(bill);
    }

    @Transactional
    public BillResponse update(Long id, UpdateBillRequest request, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));
        if (!bill.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bill not found");
        }

        if (request.getBillNumber() != null && !request.getBillNumber().isBlank()) {
            String updatedBillNumber = request.getBillNumber().trim();
            if (!updatedBillNumber.equals(bill.getBillNumber()) && billRepository.existsByBillNumber(updatedBillNumber)) {
                throw new IllegalArgumentException("Bill number already exists: " + updatedBillNumber);
            }
            bill.setBillNumber(updatedBillNumber);
        }

        bill.setTitle(request.getTitle().trim());
        bill.setAmount(request.getAmount());
        bill.setIssueDate(request.getIssueDate());
        bill.setDueDate(request.getDueDate());
        if (request.getStatus() != null) {
            bill.setStatus(request.getStatus());
        }
        bill.setNotes(request.getNotes());

        bill = billRepository.save(bill);
        return BillResponse.fromEntity(bill);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));
        if (!bill.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bill not found");
        }
        billRepository.delete(bill);
    }

    private String generateBillNumber(Long userId) {
        String datePart = LocalDate.now().format(NUMBER_DATE_FORMAT);
        long count = billRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().count();
        return BILL_NUMBER_PREFIX + datePart + "-" + (count + 1);
    }
}
