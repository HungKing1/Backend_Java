package com.request;

import lombok.Data;

@Data
public class CreditCardRefundRequest {
    private String captureId;    
    private String receiverEmail; 
}