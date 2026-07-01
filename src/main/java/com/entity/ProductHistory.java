/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: Product entity
* - Lý do: ProductHistory có relationship Many-to-One với Product thông qua
* @ManyToOne annotation. Mỗi history record reference đến một Product để
* track changes. Chỉ share Product reference (foreign key) mà không can thiệp
* vào internal logic của Product.
*
* - Mức độ: Common Coupling
* - Với lớp nào: JPA framework, Lombok annotations
* - Lý do: Sử dụng JPA annotations (@Entity, @ManyToOne, @JoinColumn) để
* define persistence mapping và Lombok annotations để generate boilerplate code.
* Framework coupling được chấp nhận trong architecture.
*
* 2. COHESION:
* - Mức độ: Informational Cohesion
* - Giữa các thành phần: id, product, action, oldValue, newValue, changedBy,
* changedDate, reason
* - Lý do: Class đại diện cho audit trail của Product changes. Tất cả attributes
* đều phục vụ mục đích tracking product modifications (what changed, old/new values,
* who changed, when, why). Các fields hoạt động cùng nhau để cung cấp complete
* audit information cho một product change event.
* ---------------------------------------------------------
*/

package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "product_historys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(nullable = true, length = 20)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_by")
    private Integer changedBy;

    @Column(name = "changed_date", nullable = true)
    private LocalDateTime changedDate;

    @Column(length = 255)
    private String reason;
}
