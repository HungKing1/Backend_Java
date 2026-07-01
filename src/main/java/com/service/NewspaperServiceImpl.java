package com.service;

import com.entity.Newspaper;
import com.entity.ProductHistory;
import com.repository.NewspaperRepository;
import com.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewspaperServiceImpl implements NewspaperService {
    @Autowired
    private NewspaperRepository newspaperRepository;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Override
    public List<Newspaper> findAll() {
        return newspaperRepository.findAll();
    }

    @Override
    public Newspaper findByProductId(Integer id) {
        Optional<Newspaper> newspaper = newspaperRepository.findByProductId(id);
        return newspaper.orElse(null);
    }

    @Override
    public Newspaper save(Newspaper newspaper) {
        // Auto-set category to "Newspaper" if not already set
        if (newspaper.getCategory() == null || newspaper.getCategory().isEmpty()) {
            newspaper.setCategory("Newspaper");
        }
        newspaper.setUpdatedAt(java.time.LocalDateTime.now());
        return newspaperRepository.save(newspaper);
    }

    public Newspaper saveWithHistory(Newspaper newspaper, String adjustmentReason, Integer changedBy) {
        // Get old Newspaper data if it exists
        Newspaper oldNewspaper = findByProductId(newspaper.getProductId());

        // Auto-set category to "Newspaper" if not already set
        if (newspaper.getCategory() == null || newspaper.getCategory().isEmpty()) {
            newspaper.setCategory("Newspaper");
        }
        newspaper.setUpdatedAt(java.time.LocalDateTime.now());

        // Save the Newspaper
        Newspaper savedNewspaper = newspaperRepository.save(newspaper);

        // Save history if stock quantity changed
        if (oldNewspaper != null && !oldNewspaper.getStockQuantity().equals(newspaper.getStockQuantity())) {
            ProductHistory history = ProductHistory.builder()
                    .product(savedNewspaper)
                    .action("UPDATE_STOCK")
                    .oldValue(oldNewspaper.getStockQuantity().toString())
                    .newValue(newspaper.getStockQuantity().toString())
                    .changedBy(changedBy)
                    .changedDate(LocalDateTime.now())
                    .reason(adjustmentReason)
                    .build();
            productHistoryRepository.save(history);
        }

        return savedNewspaper;
    }

    @Override
    public void deleteByProductId(Integer id) {
        newspaperRepository.deleteByProductId(id);
    }
}
