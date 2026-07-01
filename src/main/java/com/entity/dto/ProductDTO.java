/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductVariantDTO (through List<ProductVariantDTO>)
* - Lý do: ProductDTO contains a list of ProductVariantDTO để represent
* product variants. Chỉ là data aggregation mà không có control logic.
* ProductService sử dụng DTO này để transfer data từ entity sang client.
*
* - Mức độ: Common Coupling
* - Với lớp nào: Lombok @Data annotation
* - Lý do: Sử dụng @Data để auto-generate getters, setters, toString, equals,
* hashCode. Framework coupling được chấp nhận.
*
* 2. COHESION:
* - Mức độ: Informational Cohesion
* - Giữa các thành phần: productId, title, description, weight, currentPrice,
* supportRushOrder, categoryName, brandName, variants
* - Lý do: DTO chứa tất cả product information cần thiết cho client presentation.
* Các fields represent complete product data bao gồm basic info (title, description),
* pricing (currentPrice), categorization (categoryName, brandName), variants,
* và business rules (supportRushOrder). Tất cả fields phục vụ mục đích
* data transfer cho Product entity.
* ---------------------------------------------------------
*/

package com.entity.dto;

import lombok.Data;

import java.util.List;


@Data
public class ProductDTO {
    private Integer productId;
    private String title;
    private String description;
    private Double weight;
    private Double currentPrice;
    private String imageUrl;
    private String category;
    private Double price;
}
