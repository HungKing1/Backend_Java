package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @Column(nullable = true)
    private Double amount;

    @Column(name = "transaction_date", nullable = true)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = true, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private TransactionStatus status;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    public enum PaymentMethod {
        VIETQR,
        PAYPAL
    }

    public enum TransactionStatus {
        PENDING,
        CONFIRMED,
        FAILED,
        CANCELLED
    }
}
