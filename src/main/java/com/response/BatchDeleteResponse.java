package com.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDeleteResponse {
    private Integer totalRequested;
    private Integer deleted;        // Actually deleted from DB (stock == 0)
    private Integer deactivated;    // Deactivated (stock > 0)
    private Integer failed;         // Not found or other errors
    private Integer limitExceeded;  // Rejected due to daily or batch limit
    private String message;
    private List<ProductDeletionDetail> details;
}
