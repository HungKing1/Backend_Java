package com.service;

import com.entity.DVD;
import com.entity.ProductHistory;
import com.repository.DVDRepository;
import com.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DVDServiceImpl implements DVDService {
    @Autowired
    private DVDRepository dvdRepository;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Override
    public List<DVD> findAll() {
        return dvdRepository.findAll();
    }

    @Override
    public DVD findByProductId(Integer id) {
        Optional<DVD> dvd = dvdRepository.findByProductId(id);
        return dvd.orElse(null);
    }

    @Override
    public DVD save(DVD dvd) {
        // Auto-set category to "DVD" if not already set
        if (dvd.getCategory() == null || dvd.getCategory().isEmpty()) {
            dvd.setCategory("DVD");
        }
        dvd.setUpdatedAt(java.time.LocalDateTime.now());
        return dvdRepository.save(dvd);
    }

    public DVD saveWithHistory(DVD dvd, String adjustmentReason, Integer changedBy) {
        // Get old DVD data if it exists
        DVD oldDvd = findByProductId(dvd.getProductId());

        // Auto-set category to "DVD" if not already set
        if (dvd.getCategory() == null || dvd.getCategory().isEmpty()) {
            dvd.setCategory("DVD");
        }
        dvd.setUpdatedAt(java.time.LocalDateTime.now());

        // Save the DVD
        DVD savedDvd = dvdRepository.save(dvd);

        // Save history if stock quantity changed
        if (oldDvd != null && !oldDvd.getStockQuantity().equals(dvd.getStockQuantity())) {
            ProductHistory history = ProductHistory.builder()
                    .product(savedDvd)
                    .action("UPDATE_STOCK")
                    .oldValue(oldDvd.getStockQuantity().toString())
                    .newValue(dvd.getStockQuantity().toString())
                    .changedBy(changedBy)
                    .changedDate(LocalDateTime.now())
                    .reason(adjustmentReason)
                    .build();
            productHistoryRepository.save(history);
        }

        return savedDvd;
    }

    @Override
    public void deleteByProductId(Integer id) {
        dvdRepository.deleteByProductId(id);
    }
}
