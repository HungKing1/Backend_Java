package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String type; // "INVALID_INPUT", "PRICE_VIOLATION", "STOCK_WARNING"

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "read_date")
    private LocalDateTime readDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}
