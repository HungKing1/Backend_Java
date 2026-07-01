package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "total_price", nullable = true)
    private Double totalPrice;

    @Column(name = "item_count", nullable = true)
    private Integer itemCount;

    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDate;

    @Column(name = "last_updated", nullable = true)
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items;
}
