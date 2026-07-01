package com.request;

import lombok.Data;

@Data
public class InvoiceItemRequest {
    private Integer productId;
    private Integer quantity;
}
