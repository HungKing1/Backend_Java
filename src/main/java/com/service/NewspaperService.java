package com.service;

import com.entity.Newspaper;
import java.util.List;

public interface NewspaperService {
    List<Newspaper> findAll();

    Newspaper findByProductId(Integer id);

    Newspaper save(Newspaper newspaper);

    void deleteByProductId(Integer id);

    Newspaper saveWithHistory(Newspaper newspaper, String adjustmentReason, Integer changedBy);
}
