/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductService (consumer of this DTO)
* - Lý do: Class là Data Transfer Object dùng để truyền search/filter criteria
* từ client đến server. ProductService sử dụng DTO này để filter products
* based on multiple criteria. Chỉ chứa data fields, không có business logic.
*
* - Mức độ: Common Coupling
* - Với lớp nào: Lombok @Data annotation
* - Lý do: Sử dụng @Data để auto-generate getters, setters, toString, equals,
* hashCode. Framework coupling được chấp nhận.
*
* 2. COHESION:
* - Mức độ: Logical Cohesion
* - Giữa các thành phần: type, brandName, brandId, lowerBound, upperBound,
* storage, cpu, memory, displayResolution, refreshRate
* - Lý do: Class nhóm các filter criteria cho product search. Các fields
* liên quan đến filtering nhưng được sử dụng trong contexts khác nhau.
* Một số searches chỉ dùng price range (lowerBound, upperBound), một số
* filter by specs (cpu, memory, storage), một số by brand. Không phải tất cả
* fields đều được dùng cùng lúc trong mọi search operation. Class group
* các fields theo logic "search criteria" nhưng mức độ liên kết giữa chúng
* không cao (không phải tất cả đều cần thiết cho mọi type of search).
* ---------------------------------------------------------
*/

package com.request;

import lombok.Data;

import java.util.List;

@Data
public class SearchFilterRequest {

    private Long lowerBound;

    private Long upperBound;

    // Additional fields for comprehensive product search and filter
    private String searchTerm;

    private String category;

    private String sort;

    private Integer page;

    private Integer limit;
}
