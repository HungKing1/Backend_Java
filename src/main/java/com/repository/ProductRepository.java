/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: Product entity
* - Lý do: Repository interface làm việc với Product entity để thực hiện
* database operations. Chỉ nhận/trả về Product objects hoặc primitive types
* (Integer, String) mà không chia sẻ internal structure hay control logic.
*
* - Mức độ: Common Coupling
* - Với lớp nào: Spring Data JPA
* - Lý do: Interface extends JpaRepository và JpaSpecificationExecutor để
* kế thừa standard CRUD operations và specification-based queries. Đây là
* framework coupling cần thiết trong Spring Data JPA architecture.
*
* 2. COHESION:
* - Mức độ: Functional Cohesion
* - Giữa các thành phần: findByProductId(), findBytitle(),
* findBytitleContainingIgnoreCase() và inherited CRUD methods
* - Lý do: Tất cả methods đều phục vụ một mục đích duy nhất là data access
* cho Product entity. Các custom query methods (findByProductId, findBytitle,
* findBytitleContainingIgnoreCase) cùng với inherited methods (save, findAll,
* deleteById) đều tập trung vào việc thực hiện database operations cho Product.
* ---------------------------------------------------------
*/

package com.repository;

import com.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductId(Integer productId);

    Optional<Product> findBytitle(String title);

    // List<Product> findByCategoryId(Integer categoryId);

    // List<Product> findByBrandId(Integer brandId);

    List<Product> findBytitleContainingIgnoreCase(String title);

    List<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // List<Product> findProductsByCategoryIdAndBrandId(Integer categoryId, Integer
    // brandId);

    // List<Product> findProductsByPriceInRange(Long lowerBound, Long upperBound);
}
