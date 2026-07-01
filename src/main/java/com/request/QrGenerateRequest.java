package com.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QrGenerateRequest {
    private int invoiceId;
    private int amount;
}
