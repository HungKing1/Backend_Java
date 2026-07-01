package com.service;

import com.IPayment.ICreditCardSubsystem;
import com.entity.Invoice;
import com.entity.Transaction;
import com.repository.InvoiceRepository;
import com.repository.TransactionRepository;
import com.request.CreditCardCaptureRequest;
import com.request.CreditCardCreateRequest;
import com.request.CreditCardRefundRequest;
import com.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class PayPalSubsystem implements ICreditCardSubsystem {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    private String getAccessToken() {
        String url = baseUrl + "/v1/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                 throw new RuntimeException("Không lấy được Access Token");
            }
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
             throw new RuntimeException("Lỗi kết nối PayPal Identity: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponse> createOrder(CreditCardCreateRequest requestData) {
        try {
            Double amount = requestData.getAmount();
            
            if (amount == null || amount <= 0) {
                 return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Số tiền không hợp lệ", null)
                );
            }

            String accessToken = getAccessToken();
            String url = baseUrl + "/v2/checkout/orders";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("currency_code", "USD");
            amountMap.put("value", String.format(Locale.US, "%.2f", amount));

            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("amount", amountMap);

            Map<String, Object> payload = new HashMap<>();
            payload.put("intent", "CAPTURE");
            payload.put("purchase_units", Collections.singletonList(purchaseUnit));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return ResponseEntity.ok(
                    new ApiResponse(true, "Tạo đơn hàng PayPal thành công", response.getBody())
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Lỗi tạo đơn hàng: " + e.getMessage(), null)
            );
        }
    }

    @Override
    public ResponseEntity<ApiResponse> captureOrder(CreditCardCaptureRequest requestData) {
        try {
            String orderId = requestData.getOrderID();
            Integer invoiceId = requestData.getInvoiceId();
            
            if (orderId == null || invoiceId == null) {
                 return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Thiếu thông tin orderId hoặc invoiceId", null)
                );
            }

            String accessToken = getAccessToken();
            String url = baseUrl + "/v2/checkout/orders/" + orderId + "/capture";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("", headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "COMPLETED".equals(responseBody.get("status"))) {
                Map<String, Object> finalResult = new HashMap<>(responseBody);

                Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
                invoiceOptional.ifPresent(invoice -> {
                    invoice.setStatus(Invoice.InvoiceStatus.CONFIRMED);
                    invoiceRepository.save(invoice);
                    
                    Transaction transaction = Transaction.builder()
                        .amount(invoice.getTotalAmount())
                        .transactionDate(LocalDateTime.now())
                        .paymentMethod(Transaction.PaymentMethod.PAYPAL)
                        .status(Transaction.TransactionStatus.CONFIRMED)
                        .responseData("PayPal Capture: " + orderId)
                        .build();
                    
                    Transaction savedTrans = transactionRepository.save(transaction);
                    finalResult.put("transaction", savedTrans);
                });

                return ResponseEntity.ok(
                        new ApiResponse(true, "Thanh toán thành công!", finalResult)
                );
            } else {
                return ResponseEntity.badRequest().body(
                        new ApiResponse(false, "Thanh toán thất bại hoặc chưa hoàn tất.", responseBody)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Lỗi xử lý thanh toán: " + e.getMessage(), null)
            );
        }
    }

    @Override
    public ResponseEntity<ApiResponse> refundPayment(CreditCardRefundRequest requestData) {
        String captureId = requestData.getCaptureId();
        String receiverEmail = requestData.getReceiverEmail();

        if (captureId == null || captureId.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Vui lòng cung cấp captureId", null)
            );
        }

        try {
            String accessToken = getAccessToken();
            String url = baseUrl + "/v2/payments/captures/" + captureId + "/refund";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "COMPLETED".equals(responseBody.get("status"))) {
                if (receiverEmail != null && !receiverEmail.isEmpty()) {
                    try {
                        String subject = "Xác nhận hoàn tiền giao dịch #" + captureId;
                        StringBuilder content = new StringBuilder();
                        String refundId = (String) responseBody.get("id"); 
                        
                        content.append("Chào bạn,\n\n");
                        content.append("Yêu cầu hoàn tiền của bạn đã được xử lý thành công.\n");
                        content.append("Thông tin chi tiết:\n");
                        content.append("- Mã giao dịch gốc (Capture ID): ").append(captureId).append("\n");
                        content.append("- Mã hoàn tiền (Refund ID): ").append(refundId != null ? refundId : "N/A").append("\n");
                        content.append("- Thời gian xử lý: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");
                        content.append("Cảm ơn bạn đã sử dụng dịch vụ.\n");

                        emailService.sendEmail(receiverEmail, subject, content.toString());
                        
                    } catch (Exception e) {
                        System.err.println("Cảnh báo: Hoàn tiền thành công nhưng gửi email thất bại.");
                    }
                }

                 return ResponseEntity.ok(
                        new ApiResponse(true, "Hoàn tiền thành công!", responseBody)
                );
            } else {
                 return ResponseEntity.badRequest().body(
                        new ApiResponse(false, "Hoàn tiền thất bại.", responseBody)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Lỗi khi gọi hoàn tiền: " + e.getMessage(), null)
            );
        }
    }
}