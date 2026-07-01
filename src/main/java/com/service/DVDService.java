package com.service;

import com.entity.DVD;
import java.util.List;

public interface DVDService {
    List<DVD> findAll();

    DVD findByProductId(Integer id);

    DVD save(DVD dvd);

    void deleteByProductId(Integer id);

    DVD saveWithHistory(DVD dvd, String adjustmentReason, Integer changedBy);
}
