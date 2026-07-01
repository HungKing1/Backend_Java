package com.service;

import com.entity.CD;
import com.entity.ProductHistory;
import com.repository.CDRepository;
import com.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CDServiceImpl implements CDService {
    @Autowired
    private CDRepository cdRepository;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Override
    public List<CD> findAll() {
        return cdRepository.findAll();
    }

    @Override
    public CD findByProductId(Integer id) {
        Optional<CD> cd = cdRepository.findByProductId(id);
        return cd.orElse(null);
    }

    @Override
    public CD save(CD cd) {
        // Auto-set category to "CD" if not already set
        if (cd.getCategory() == null || cd.getCategory().isEmpty()) {
            cd.setCategory("CD");
        }
        cd.setUpdatedAt(java.time.LocalDateTime.now());
        return cdRepository.save(cd);
    }

    public CD saveWithHistory(CD cd, String adjustmentReason, Integer changedBy) {

        CD oldCd = findByProductId(cd.getProductId());
        Integer oldStock = oldCd != null ? oldCd.getStockQuantity() : null;

        if (cd.getCategory() == null || cd.getCategory().isEmpty()) {
            cd.setCategory("CD");
        }
        cd.setUpdatedAt(LocalDateTime.now());

        CD savedCd = cdRepository.save(cd);

        if (oldStock != null && !oldStock.equals(cd.getStockQuantity())) {
            ProductHistory history = ProductHistory.builder()
                    .product(savedCd)
                    .action("UPDATE_STOCK")
                    .oldValue(oldStock.toString())
                    .newValue(cd.getStockQuantity().toString())
                    .changedBy(changedBy)
                    .changedDate(LocalDateTime.now())
                    .reason(adjustmentReason)
                    .build();

            productHistoryRepository.save(history);
        }

        return savedCd;
    }

    @Override
    public void deleteByProductId(Integer id) {
        cdRepository.deleteByProductId(id);
    }
}
