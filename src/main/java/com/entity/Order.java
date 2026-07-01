package com.entity;

import com.enums.OrderStatus;
import com.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.request.OrderRequest;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "receiver_name", length = 255, nullable = false)
    private String receiverName;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "shipping_address", length = 500, nullable = false)
    private String shippingAddress;

    @Column(name = "shipping_fee", nullable = false)
    private Double shippingFee;

    @Column(nullable = false)
    private Double subtotal;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "shipping_instructions", columnDefinition = "TEXT")
    private String shippingInstructions;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_info_id")
    private DeliveryInfo deliveryInfo;

    @Column(name = "transaction_id")
    private Integer transactionId;

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "invoice_id", unique = true)
    private Invoice invoice;

    // === CONSTRUCTOR ===
    public Order(OrderRequest orderRequest, Integer userId) {
        this.userId = userId;
        this.receiverName = orderRequest.getReceiverName();
        this.phoneNumber = orderRequest.getPhoneNumber();
        this.shippingAddress = orderRequest.getShippingAddress();
        this.paymentMethod = orderRequest.getPaymentMethod();
        this.shippingFee = (double) orderRequest.getShippingFee();
        this.note = orderRequest.getNote();
        this.shippingInstructions = orderRequest.getShippingInstructions();
        this.createdAt = LocalDateTime.now();
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    // === PRE PERSIST ===
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();
        if (this.orderDate == null)
            this.orderDate = LocalDateTime.now();
    }
}