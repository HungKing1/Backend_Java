package com.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeletionDetail {
    private Integer productId;
    private String productTitle;
    private String status; // DELETED, DEACTIVATED, FAILED, LIMIT_EXCEEDED
    private String reason;
}
