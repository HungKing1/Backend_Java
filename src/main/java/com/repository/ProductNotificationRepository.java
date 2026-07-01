package com.repository;

import com.entity.ProductNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductNotificationRepository extends JpaRepository<ProductNotification, Integer> {

    /**
     * Find unread notifications
     */
    List<ProductNotification> findByIsReadFalseOrderByCreatedDateDesc();

    /**
     * Find all notifications for a product
     */
    List<ProductNotification> findByProduct_ProductIdOrderByCreatedDateDesc(Integer productId);

    /**
     * Find notifications by type
     */
    List<ProductNotification> findByTypeOrderByCreatedDateDesc(String type);

    /**
     * Find unread notifications for a product
     */
    List<ProductNotification> findByProduct_ProductIdAndIsReadFalseOrderByCreatedDateDesc(Integer productId);

    /**
     * Count unread notifications
     */
    @Query("SELECT COUNT(pn) FROM ProductNotification pn WHERE pn.isRead = false")
    long countUnreadNotifications();

    /**
     * Find recent notifications (last 24 hours)
     */
    @Query("SELECT pn FROM ProductNotification pn WHERE pn.createdDate >= :sinceDate ORDER BY pn.createdDate DESC")
    List<ProductNotification> findRecentNotifications(@Param("sinceDate") LocalDateTime sinceDate);
}
