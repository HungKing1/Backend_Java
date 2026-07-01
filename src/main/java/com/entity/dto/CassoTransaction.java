package com.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CassoTransaction {
    @JsonProperty("id")
    private int id;

    @JsonProperty("tid")
    private String tid; // Mã giao dịch ngân hàng

    @JsonProperty("description")
    private String description; // Nội dung chuyển khoản

    @JsonProperty("amount")
    private long amount; // Số tiền

    @JsonProperty("when")
    private String when; // Thời gian

    @JsonProperty("bankSubAccId")
    private String bankSubAccId; // Tài khoản ngân hàng nhận
}