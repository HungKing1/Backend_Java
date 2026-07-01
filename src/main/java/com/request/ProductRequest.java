/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductService (consumer of this DTO)
* - Lý do: Class này là Data Transfer Object (DTO) được sử dụng để truyền
* dữ liệu từ client đến server trong các API requests. Chỉ chứa data fields
* mà không có business logic. ProductService sử dụng object này để nhận
* input parameters cho create/update operations.
*
* 2. COHESION:
* - Mức độ: Logical Cohesion
* - Giữa các thành phần: productId, title, description, weight, currentPrice,
* categoryName, brandName, supportRushOrder, isActive
* - Lý do: Tất cả fields đều liên quan đến Product nhưng được sử dụng trong
* các contexts khác nhau. Một số fields cho create operation (title, description),
* một số cho update (productId, isActive), và một số optional (categoryName,
* brandName). Class nhóm các fields theo logic "Product data" nhưng không phải
* tất cả fields đều được dùng cùng lúc trong mọi use case.
* ---------------------------------------------------------
*/

package com.request;

import lombok.Data;

@Data
public class ProductRequest {

    private Integer productId;

    private String title;

    private String description;

    private Double weight;

    private Double originalPrice;

    private Double currentPrice;

    private String categoryName;

    private String brandName;

    private Boolean supportRushOrder;

    private Boolean isActive;

    private Integer stockQuantity;

    private String adjustmentReason;

}
