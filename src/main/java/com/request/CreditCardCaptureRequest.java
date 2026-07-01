package com.request;

import lombok.Data;

@Data
public class CreditCardCaptureRequest {
    private String orderID;   
    private Integer invoiceId; 
}