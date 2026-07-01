package com.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculateShippingFeeTest {

    @InjectMocks
    private ShippingService controller;

    @BeforeEach
    void setUp() {
        controller = new ShippingService();
    }

    @DisplayName("Test tính phí vận chuyển theo cân nặng, vùng miền và giá trị đơn hàng")
    @ParameterizedTest(name = "{index} => weight={0}, address={1}, orderValue={2}, expected={3}")
    @CsvSource({
            // --- Nội thành (Hà Nội/Hồ Chí Minh) ---
            // Base fee: 22k cho 3kg đầu
            "1.2, Hà Nội, 50000, 22000",       // < 3kg: 22k
            "3.0, Hồ Chí Minh, 50000, 22000",  // = 3kg: 22k (Biên)
            "3.5, Hà Nội, 50000, 24500",       // > 3kg: 22k + 2.5k (0.5kg tiếp theo)

            // --- Ngoại thành (Tỉnh khác) ---
            // Base fee: 30k cho 0.5kg đầu
            "0.4, Đà Nẵng, 50000, 30000",      // < 0.5kg: 30k
            "0.5, Huế, 50000, 30000",          // = 0.5kg: 30k (Biên)
            "1.0, Cần Thơ, 50000, 32500",      // > 0.5kg: 30k + 2.5k (0.5kg tiếp theo)

            // --- Logic Free Ship (Đơn > 100k, Giảm max 25k) ---
            "1.0, Hà Nội, 150000, 0",          // Phí gốc 22k -> Giảm 25k -> Còn 0đ
            "10.0, Hà Nội, 150000, 32000",     // 10kg dư 7kg (14 nấc). Phí: 22k + (14*2.5k) = 57k. Giảm 25k -> Còn 32k
            "0.5, Đà Nẵng, 150000, 5000",      // Phí gốc 30k -> Giảm 25k -> Còn 5k

            // --- Case Lỗi (Validation) ---
            "-1.0, Hà Nội, 50000, -1",         // Cân nặng âm -> Trả về -1
            "2.0, , 50000, -1"                 // Địa chỉ rỗng/null -> Trả về -1
    })
    void testCalculateShippingFee(double weight, String shippingAddress, double orderValue, int expectedFee) {
        int actualFee = controller.calculateShippingFee(shippingAddress, weight, orderValue);
        assertEquals(expectedFee, actualFee);
    }
}