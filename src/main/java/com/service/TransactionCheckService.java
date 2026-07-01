/*
 * ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
 * ---------------------------------------------------------
 * 1. COUPLING :
 * - Mức độ: Data Coupling
 * - Với lớp nào: Các lớp gọi hàm checkPaid
 * - Lý do: Phương thức `checkPaid` chỉ nhận các tham số kiểu nguyên thủy đơn giản
 * (int amount, int invoiceId) để thực hiện chức năng. Toàn bộ dữ liệu truyền vào
 * đều được sử dụng hết[cite: 215]. Hàm không yêu cầu truyền cả một đối tượng phức tạp
 * (như Invoice hay Order) chỉ để lấy ra một trường ID (tránh được Stamp Coupling).
 *
 * 2. COHESION:
 * - Mức độ: Functional Cohesion
 * - Giữa các thành phần: Các dòng lệnh logic trong hàm `checkPaid` và các thuộc tính (CASSO_API_KEY).
 * - Lý do: Toàn bộ nội dung của class và hàm này chỉ tập trung vào duy nhất một nhiệm vụ
 * cụ thể: "Kiểm tra tính hợp lệ của giao dịch với bên thứ 3". Mọi dòng code (tạo header,
 * gọi API, so sánh chuỗi) đều thiết yếu cho một tính toán duy nhất này.
 * Không có code thừa hay chức năng không liên quan bị trộn lẫn.
 * ---------------------------------------------------------
 */
package com.service;

import com.entity.dto.CassoResponse;
import com.entity.dto.CassoTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionCheckService {

    @Value("${casso.api.key}")
    private String CASSO_API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Kiểm tra thanh toán dựa trên số tiền và mã hóa đơn
     * @param amount Số tiền cần thanh toán
     * @param invoiceId Mã hóa đơn (Ví dụ: 101, 102...)
     * @return true nếu tìm thấy giao dịch hợp lệ
     */
    public boolean checkPaid(int amount, int invoiceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Apikey " + CASSO_API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<CassoResponse> response = restTemplate.exchange(
                    "https://oauth.casso.vn/v2/transactions?pageSize=10&sort=DESC",
                    HttpMethod.GET,
                    entity,
                    CassoResponse.class
            );

            String expectedDescription = "Thanh toan hoa don " + invoiceId;

            if (response.getBody() != null && response.getBody().getData() != null) {
                for (CassoTransaction transaction : response.getBody().getData().getRecords()) {

                    if (transaction.getAmount() >= amount &&
                            transaction.getDescription().toUpperCase().contains(expectedDescription.toUpperCase())) {

                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}