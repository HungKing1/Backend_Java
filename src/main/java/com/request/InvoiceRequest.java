package com.request;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceRequest {
    private List<InvoiceItemRequest> items;
    private int shippingFee;
}
