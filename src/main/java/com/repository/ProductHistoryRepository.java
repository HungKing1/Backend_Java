package com.repository;

import com.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Integer> {

       /**
        * Count deletion actions (DELETE_PRODUCT or DEACTIVATE_PRODUCT) within a date
        * range
        * Used to enforce daily deletion limit (max 20 per day)
        */
       @Query("SELECT COUNT(ph) FROM ProductHistory ph WHERE ph.action IN ('DELETE_PRODUCT', 'DEACTIVATE_PRODUCT') " +
                     "AND ph.changedDate BETWEEN :startDate AND :endDate")
       long countDeletionsByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Get product history by product ID
        */
       List<ProductHistory> findByProduct_ProductId(Integer productId);
}
