package com.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CassoResponse {
    @JsonProperty("error")
    private int error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private CassoData data;
}