package com.example.backend.service;

import com.example.backend.dto.CreateInvoiceRequest;
import com.example.backend.dto.InvoiceDetailDto;
import com.example.backend.dto.InvoiceResponse;
import com.example.backend.dto.UpdateInvoiceRequest;
import com.example.backend.entity.Invoice;
import com.example.backend.entity.Invoice.InvoiceStatus;
import com.example.backend.entity.User;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private static final Long USER_ID = 1L;
    private User user;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("user@example.com");

        invoice = new Invoice();
        invoice.setId(10L);
        invoice.setUser(user);
        invoice.setInvoiceNumber("INV-20250101-1");
        invoice.setIssueDate(LocalDate.of(2025, 1, 1));
        invoice.setDueDate(LocalDate.of(2025, 1, 31));
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setSubtotal(BigDecimal.valueOf(100.00));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotal(BigDecimal.valueOf(100.00));
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("returns list of InvoiceResponse for user")
        void returnsList() {
            when(invoiceRepository.findByUser_IdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(invoice));

            List<InvoiceResponse> result = invoiceService.findAllByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-20250101-1");
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("throws when invoice not found")
        void notFound_throws() {
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.findById(99L, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invoice not found");
        }

        @Test
        @DisplayName("throws when invoice belongs to another user")
        void wrongUser_throws() {
            when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> invoiceService.findById(10L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invoice not found");
        }

        @Test
        @DisplayName("returns InvoiceResponse when owner matches")
        void success() {
            when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));

            InvoiceResponse result = invoiceService.findById(10L, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getInvoiceNumber()).isEqualTo("INV-20250101-1");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("throws when user not found")
        void userNotFound_throws() {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setIssueDate(LocalDate.now());
            request.setDueDate(LocalDate.now().plusDays(30));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.create(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("throws when invoice number already exists")
        void duplicateInvoiceNumber_throws() {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setInvoiceNumber("INV-001");
            request.setIssueDate(LocalDate.now());
            request.setDueDate(LocalDate.now().plusDays(30));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(invoiceRepository.existsByInvoiceNumber("INV-001")).thenReturn(true);

            assertThatThrownBy(() -> invoiceService.create(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invoice number already exists: INV-001");
        }

        @Test
        @DisplayName("saves invoice with details")
        void success() {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setIssueDate(LocalDate.of(2025, 2, 1));
            request.setDueDate(LocalDate.of(2025, 2, 28));
            InvoiceDetailDto detailDto = new InvoiceDetailDto();
            detailDto.setDescription("Item A");
            detailDto.setQuantity(BigDecimal.valueOf(2));
            detailDto.setUnitPrice(BigDecimal.valueOf(50.00));
            request.setDetails(List.of(detailDto));

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
            when(invoiceRepository.findByUser_IdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of());
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
                Invoice invEntity = inv.getArgument(0);
                invEntity.setId(1L);
                return invEntity;
            });

            InvoiceResponse result = invoiceService.create(request, USER_ID);

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(any(Invoice.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when invoice not found")
        void notFound_throws() {
            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setIssueDate(LocalDate.now());
            request.setDueDate(LocalDate.now().plusDays(30));
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.update(99L, request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invoice not found");
        }

        @Test
        @DisplayName("updates and saves invoice")
        void success() {
            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setIssueDate(LocalDate.of(2025, 3, 1));
            request.setDueDate(LocalDate.of(2025, 3, 31));
            request.setStatus(InvoiceStatus.SENT);
            when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

            InvoiceResponse result = invoiceService.update(10L, request, USER_ID);

            assertThat(result).isNotNull();
            assertThat(invoice.getIssueDate()).isEqualTo(LocalDate.of(2025, 3, 1));
            assertThat(invoice.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 31));
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
            verify(invoiceRepository).save(invoice);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when invoice not found")
        void notFound_throws() {
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.delete(99L, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invoice not found");
        }

        @Test
        @DisplayName("deletes when owner matches")
        void success() {
            when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));

            invoiceService.delete(10L, USER_ID);

            verify(invoiceRepository).delete(invoice);
        }
    }
}
