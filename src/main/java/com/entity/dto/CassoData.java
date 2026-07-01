package com.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CassoData {
    @JsonProperty("page")
    private int page;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("records")
    private List<CassoTransaction> records;

}