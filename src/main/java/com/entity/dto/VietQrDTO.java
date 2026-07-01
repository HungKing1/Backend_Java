package com.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VietQrDTO {
    // Dữ liệu tạo QR
    private String orderId;
    private Long amount;
    private String orderInfo;
    private String bankCode; // Ví dụ: VCB, MB...

    // Dữ liệu khi Callback trả về
    private String transactionId;
    private String status; // "00" là thành công
    private String error;
}