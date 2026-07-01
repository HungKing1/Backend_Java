/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductHistory, CartItem, OrderItem
* - Lý do: Product entity có quan hệ One-to-Many với ProductHistory, CartItem,
* và OrderItem thông qua JPA mappings. Các relationships này chỉ share data
* references (foreign keys) mà không chia sẻ control logic hay internal structure.
*
* 2. COHESION:
* - Mức độ: Informational Cohesion
* - Giữa các thành phần: productId, barcode, title, category, description, imageUrl,
* dimensions, weight, originalPrice, currentPrice, stockQuantity, status,
* createdAt, updatedAt, isActive, histories, cartItems, orderItems
* - Lý do: Class đại diện cho Product entity với tất cả attributes và relationships
* liên quan đến một sản phẩm. Các fields mô tả đầy đủ thông tin sản phẩm
* (basic info, pricing, inventory, metadata, relationships). Lifecycle callbacks
* (prePersist, preUpdate) cũng phục vụ quản lý trạng thái entity.
* ---------------------------------------------------------
*/

package com.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(nullable = true)
    private String barcode;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "category", nullable = true, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = true)
    private String dimensions;

    @Column(nullable = true)
    private Double weight;

    @Column(name = "original_price", nullable = true)
    private Double originalPrice;

    @Column(name = "current_price", nullable = true)
    private Double currentPrice;

    @Column(nullable = true)
    private Integer stockQuantity;

    @Column(nullable = true)
    private String status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "reason_for_adjustment", columnDefinition = "TEXT")
    private String reasonForAdjustment;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductHistory> histories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItems;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}