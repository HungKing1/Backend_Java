package com.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatePriceConstraintTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService();
    }

    @DisplayName("Test validate giá bán: check null, giá trị âm và khoảng giá (30% - 150%)")
    @ParameterizedTest(name = "{index} => original={0}, current={1}, expected={2}")
    @CsvSource(value = {
            // --- Case Hợp Lệ (30% <= current <= 150% original) ---
            "100000.0, 30000.0, VALID",     // Min boundary: 30% (30k) -> OK
            "100000.0, 150000.0, VALID",    // Max boundary: 150% (150k) -> OK
            "100000.0, 100000.0, VALID",    // Normal case: Bằng giá gốc -> OK
            "200000.0, 100000.0, VALID",    // 50% giá gốc -> OK

            // --- Case Lỗi: Null Check ---
            "NULL, 50000.0, Giá gốc và giá bán không được để trống",     // Original is null
            "100000.0, NULL, Giá gốc và giá bán không được để trống",    // Current is null
            "NULL, NULL, Giá gốc và giá bán không được để trống",        // Both null

            // --- Case Lỗi: Giá gốc <= 0 ---
            "0.0, 10000.0, Giá gốc phải lớn hơn 0",      // Original = 0
            "-10000.0, 10000.0, Giá gốc phải lớn hơn 0", // Original < 0

            // --- Case Lỗi: Vi phạm khoảng giá (Ngoài vùng 30% - 150%) ---
            "100000.0, 29000.0, Giá bán phải từ 30000đ (30%) đến 150000đ (150%) của giá gốc",
            "100000.0, 151000.0, Giá bán phải từ 30000đ (30%) đến 150000đ (150%) của giá gốc"
    }, nullValues = {"NULL"})
    void testValidatePriceConstraint(Double originalPrice, Double currentPrice, String expectedMessage) {
        if ("VALID".equals(expectedMessage)) {
            assertDoesNotThrow(() -> service.validatePriceConstraint(originalPrice, currentPrice));
        }
        else {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> service.validatePriceConstraint(originalPrice, currentPrice));
            assertEquals(expectedMessage, exception.getMessage());
        }
    }
}