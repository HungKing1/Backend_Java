package com.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(name = "detail_type", nullable = true, length = 50)
    private String detailType;

    @Column(name = "detail_value", columnDefinition = "TEXT", nullable = true)
    private String detailValue;
}
