package com.service;

import com.entity.CD;
import java.util.List;

public interface CDService {
    List<CD> findAll();

    CD findByProductId(Integer id);

    CD save(CD cd);

    void deleteByProductId(Integer id);

    CD saveWithHistory(CD cd, String adjustmentReason, Integer changedBy);
}
