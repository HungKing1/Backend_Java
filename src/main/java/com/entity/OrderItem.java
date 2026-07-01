package com.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_items")
@IdClass(OrderItemId.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {

    // === KHÔNG PHẢI @Id → chỉ là cột thường, tự tăng bởi MySQL ===
    @Column(name = "order_item_id", insertable = false, updatable = false)
    private Integer orderItemId;

    // === COMPOSITE KEY (phải có @Id) ===
    @Id
    @Column(name = "order_id", nullable = true)
    private Integer orderId;

    @Id
    @Column(name = "product_id", nullable = true)
    private Integer productId;

    // === CÁC FIELD KHÁC ===
    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true)
    private Double price; // giữ cho code cũ

    @Column(name = "price_decimal", nullable = true)
    private Double priceDecimal;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonIgnore
    private Product product;

    // === CONSTRUCTOR ===
    public OrderItem(Integer orderId, Integer productId, Integer quantity, Double price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}