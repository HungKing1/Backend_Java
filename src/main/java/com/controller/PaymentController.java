/*
 * ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
 * ---------------------------------------------------------
 * 1. COUPLING:
 * - Mức độ: Stamp Coupling
 * - Với lớp nào: QrGenerateRequest, IQrPayment
 * - Lý do: Controller nhận vào một cấu trúc dữ liệu phức tạp (DTO QrGenerateRequest)
 * từ RequestBody và truyền nguyên cấu trúc đó sang cho interface `qrPayment` xử lý.
 * Controller phụ thuộc vào định nghĩa của cấu trúc dữ liệu này.
 * Tuy nhiên, việc phụ thuộc vào Interface (IQrPayment) thay vì class cụ thể
 * là một điểm cộng lớn (Loose Coupling qua Subtyping).
 *
 * 2. COHESION:
 * - Mức độ: Informational Cohesion
 * - Giữa các thành phần: Các method generateQr, checkStatus và thuộc tính qrPayment.
 * - Lý do: Class này nhóm các hành động liên quan đến cùng một thực thể logic
 * là "Payment". Mỗi hành động (tạo QR, kiểm tra trạng thái) có điểm vào (entry point)
 * riêng biệt (các API endpoint khác nhau), code độc lập, nhưng cùng thao tác trên
 * cùng một miền dữ liệu/nghiệp vụ.
 * ---------------------------------------------------------
 */

package com.controller;

import com.IPayment.ICreditCardSubsystem;
import com.IPayment.IQrCodeSubsystem;
import com.request.CreditCardCaptureRequest;
import com.request.CreditCardCreateRequest;
import com.request.CreditCardRefundRequest;
import com.request.QrGenerateRequest;
import com.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    
    private final IQrCodeSubsystem qrPayment;
    private final ICreditCardSubsystem creditCardPayment;

    @PostMapping("/generate-qr")
    public ResponseEntity<ApiResponse> generateQr(@RequestBody QrGenerateRequest qrGenerateRequest) {
        return qrPayment.generateQr(qrGenerateRequest);
    }

    @PostMapping("/check-status")
    public ResponseEntity<ApiResponse> checkStatus(@RequestBody QrGenerateRequest qrGenerateRequest) {
        return qrPayment.checkStatus(qrGenerateRequest);
    }

    @PostMapping("/paypal/create")
    public ResponseEntity<ApiResponse> createOrder(@RequestBody CreditCardCreateRequest request) {
        return creditCardPayment.createOrder(request);
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse> captureOrder(@RequestBody CreditCardCaptureRequest request) {
        return creditCardPayment.captureOrder(request);
    }

    @PostMapping("/paypal/refund")
    public ResponseEntity<ApiResponse> refundOrder(@RequestBody CreditCardRefundRequest request) {
        return creditCardPayment.refundPayment(request);
    }
}