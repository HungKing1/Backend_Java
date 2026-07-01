package com.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;

    // 1 Invoice thuộc về 1 Order (sau khi confirm)
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private Order order;

    // Danh sách item trong hóa đơn
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> invoiceItems;

    // Tổng tiền chưa VAT
    @Column(nullable = false)
    private double subtotal;

    // Tổng tiền đã VAT (10%)
    @Column(nullable = false)
    private double totalWithVAT;

    // Phí vận chuyển (không VAT)
    @Column(nullable = false)
    private double deliveryFee;

    // Tổng tiền phải trả = totalWithVAT + deliveryFee
    @Column(nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // PENDING, CONFIRMED, CANCELLED

    private LocalDateTime createdAt;

    public enum InvoiceStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

}
