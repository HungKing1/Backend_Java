package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "delivery_infos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer id;

    @Column(name = "receiver_name", nullable = true, length = 255)
    private String receiverName;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(nullable = true, length = 100)
    private String province;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String address;

    @Column(name = "shipping_instructions", columnDefinition = "TEXT")
    private String shippingInstructions;

    @Column(name = "shipping_fee", nullable = true)
    private Double shippingFee;

    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "deliveryInfo")
    private List<Order> orders;
}
