package com.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cart_items")
@IdClass(CartItemId.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CartItem {

    // XÓA @GeneratedValue → vì không phải @Id
    @Column(name = "cart_item_id", insertable = false, updatable = false)
    private Integer cartItemId;

    // === COMPOSITE KEY ===
    @Id
    @Column(name = "user_id", nullable = true)
    private Integer userId;

    @Id
    @Column(name = "product_id", nullable = true)
    private Integer productId;

    @Column(name = "is_selected")
    private boolean isSelected = true;

    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true)
    private Double price;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // Constructor
    public CartItem(Integer userId, Integer productId, Integer quantity, Double price) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}