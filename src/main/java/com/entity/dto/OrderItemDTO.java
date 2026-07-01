package com.entity.dto;

import lombok.Data;

@Data
public class OrderItemDTO {

    private Integer productId;
    // private Integer variantId;
    private String title;
    private String description;
    private Double weight;
    private Double price;
    private String color;
    private String imageUrl;
    private Integer quantity;
}
