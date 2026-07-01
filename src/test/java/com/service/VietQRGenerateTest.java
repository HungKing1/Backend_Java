package com.service;

import com.request.QrGenerateRequest;
import com.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VietQRGenerateTest {

    private VietQRSubsystem vietQRSubsystem;

    @BeforeEach
    void setUp() {
        vietQRSubsystem = new VietQRSubsystem(null, null, null);
    }

    @ParameterizedTest
    @CsvSource({
            // Case 1: Hợp lệ - Số tiền dương, ID hợp lệ
            // Kỳ vọng: Success = true, URL chứa đúng format của VietQR
            "100000, 101, true, https://img.vietqr.io/image/mb-0971048630-compact2.jpg",

            // Case 2: Hợp lệ - Kiểm tra tham số amount trong URL
            // Kỳ vọng: Success = true, URL chứa tham số amount=50000
            "50000, 999, true, amount=50000",

            // Case 3: Hợp lệ - Kiểm tra nội dung chuyển khoản được encode
            // Kỳ vọng: Success = true, URL chứa addInfo đã encode (Thanh toan hoa don #999)
            "50000, 999, true, addInfo=Thanh+toan+hoa+don+%23999",

            // Case 4: Lỗi Validation - Số tiền Âm
            // Kỳ vọng: Success = false, Message lỗi cụ thể
            "-1000, 101, false, Amount must be greater than 0",

            // Case 5: Lỗi Validation - Số tiền bằng 0
            // Kỳ vọng: Success = false, Message lỗi cụ thể
            "0, 101, false, Amount must be greater than 0"
    })
    void testGenerateQr(long amount, int invoiceId, boolean expectedSuccess, String expectedContent) {
        QrGenerateRequest request = new QrGenerateRequest(invoiceId, (int) amount);

        ResponseEntity<ApiResponse> response = vietQRSubsystem.generateQr(request);
        ApiResponse body = response.getBody();

        if (expectedSuccess) {
            assertEquals(true, body.getSuccess());

            String qrUrl = (String) body.getData();
            assertTrue(qrUrl.contains(expectedContent),
                    "URL sinh ra (" + qrUrl + ") không chứa thông tin mong đợi: " + expectedContent);
        } else {
            assertEquals(false, body.getSuccess());
            assertEquals(expectedContent, body.getMessage());
        }
    }
}