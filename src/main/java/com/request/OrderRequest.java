package com.request;

import com.enums.OrderStatus;
import com.enums.PaymentMethod;
import lombok.Data;

@Data
public class OrderRequest {

    private String receiverName;

    private String phoneNumber;

    private String receiverEmail;

    private String shippingAddress;

    private String shippingInstructions;

    private PaymentMethod paymentMethod;


    private Integer shippingFee;

    private String note;

    private Item[] items;

    private Integer invoiceId;

    private Integer transactionId;
}
