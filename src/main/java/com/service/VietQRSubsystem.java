/*
 * ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
 * ---------------------------------------------------------
 * 1. COUPLING:
 * - Mức độ: Stamp Coupling
 * - Với lớp nào: QrGenerateRequest, Invoice
 * - Lý do: Các phương thức generateQr và checkStatus nhận vào toàn bộ đối tượng
 * QrGeneratecheckStatusRequest (DTO) làm tham số thay vì các dữ liệu đơn lẻ (như amount, id).
 * Hàm checkStatus cũng phụ thuộc vào cấu trúc dữ liệu của Invoice khi thực hiện get/set.
 * Module này buộc phải biết về cấu trúc nội bộ của các object này.
 *
 * - Mức độ: Data Coupling
 * - Với lớp nào: TransactionCheckService
 * - Lý do: Trong hàm checkStatus, việc gọi transactionCheckService.checkPaid(...)
 * truyền các tham số cụ thể (amount, id) để xử lý, đây là mức coupling tốt nhất.
 *
 * 2. COHESION:
 * - Mức độ: Informational Cohesion
 * - Giữa các thành phần: generateQr, checkStatus và các dependencies (Repositories)
 * - Lý do: Class này được thiết kế theo hướng đối tượng (Class/Interface), đóng vai trò
 * như một ADT quản lý "VietQR". Nó chứa nhiều hành động (generate, check), mỗi hành động
 * có điểm vào (entry point) riêng biệt, code độc lập, nhưng cùng thực hiện trên một
 * thực thể logic là "Giao dịch thanh toán VietQR".
 * ---------------------------------------------------------
 */
package com.service;

import com.IPayment.IQrCodeSubsystem;
import com.entity.Invoice;
import com.entity.Transaction;
import com.repository.InvoiceRepository;
import com.repository.TransactionRepository;
import com.request.QrGenerateRequest;
import com.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VietQRSubsystem implements IQrCodeSubsystem {
    private final TransactionCheckService transactionCheckService;
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public ResponseEntity<ApiResponse> generateQr(QrGenerateRequest qrGenerateRequest) {

        if (qrGenerateRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Amount must be greater than 0", null));
        }

        try {
            String accountNumber = "0971048630";
            String accountName = "NGUYEN MANH HUNG";
            String addInfoRaw = "Thanh toan hoa don #" + qrGenerateRequest.getInvoiceId();
            String addInfo = URLEncoder.encode(addInfoRaw, StandardCharsets.UTF_8);

            String qrUrl = String.format(
                    "https://img.vietqr.io/image/mb-%s-compact2.jpg?amount=%d&addInfo=%s&accountName=%s",
                    accountNumber,
                    qrGenerateRequest.getAmount(),
                    addInfo,
                    URLEncoder.encode(accountName, StandardCharsets.UTF_8));
            return ResponseEntity.ok(
                    new ApiResponse(true, "QR code generated successfully", qrUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, "Failed to generate QR code: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> checkStatus(QrGenerateRequest qrGenerateRequest) {
        boolean isPaid = transactionCheckService.checkPaid(qrGenerateRequest.getAmount(),
                qrGenerateRequest.getInvoiceId());
        if (isPaid) {
            Optional<Invoice> invoiceOptional = invoiceRepository.findById(qrGenerateRequest.getInvoiceId());
            invoiceOptional.ifPresent(invoice -> {
                invoice.setStatus(Invoice.InvoiceStatus.CONFIRMED);
                invoiceRepository.save(invoice);
            });

            Invoice invoice = invoiceOptional.get();
            Transaction transaction = Transaction.builder()
                    .amount(invoice.getTotalAmount())
                    .transactionDate(LocalDateTime.now())
                    .paymentMethod(Transaction.PaymentMethod.VIETQR)
                    .status(Transaction.TransactionStatus.CONFIRMED)
                    .responseData("Thanh toan don hang " + invoice.getInvoiceId())
                    .build();
            transactionRepository.save(transaction);
            return ResponseEntity.ok(new ApiResponse(true, "Thanh toán thành công!", transaction));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse(false, "Giao dịch chưa hoàn tất hoặc chưa tìm thấy", null));
        }
    }
}