package com.IPayment;

import com.request.CreditCardCaptureRequest;
import com.request.CreditCardCreateRequest;
import com.request.CreditCardRefundRequest;
import com.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ICreditCardSubsystem {
    ResponseEntity<ApiResponse> createOrder(CreditCardCreateRequest request);
    ResponseEntity<ApiResponse> captureOrder(CreditCardCaptureRequest request);
    ResponseEntity<ApiResponse> refundPayment(CreditCardRefundRequest request);
}