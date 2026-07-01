package com.IPayment;

import com.request.QrGenerateRequest;
import com.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface IQrCodeSubsystem {
    ResponseEntity<ApiResponse> generateQr(QrGenerateRequest qrGenerateRequest);
    ResponseEntity<ApiResponse> checkStatus(QrGenerateRequest qrGenerateRequest);
}
