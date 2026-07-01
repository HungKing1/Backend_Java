package com.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nhiều InvoiceItem thuộc về 1 Invoice
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    // Nhiều InvoiceItem thuộc về 1 Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Số lượng sản phẩm
    @Column(nullable = false)
    private Integer quantity;

    // Giá của 1 sản phẩm tại thời điểm tạo invoice
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    // Tổng tiền = unit_price * quantity
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    // Tự động tính totalPrice trước khi lưu
    @PrePersist
    @PreUpdate
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = this.unitPrice * this.quantity;
        }
    }
}
