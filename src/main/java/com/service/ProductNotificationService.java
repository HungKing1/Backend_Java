package com.service;

import com.entity.Product;
import com.entity.ProductNotification;
import com.repository.ProductNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductNotificationService {

    @Autowired
    private ProductNotificationRepository notificationRepository;

    /**
     * Create a new notification for invalid input
     */
    public ProductNotification createInvalidInputNotification(
            Product product,
            String title,
            String message,
            String details) {
        ProductNotification notification = ProductNotification.builder()
                .product(product)
                .type("INVALID_INPUT")
                .title(title)
                .message(message)
                .details(details)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Create a price violation notification
     */
    public ProductNotification createPriceViolationNotification(
            Product product,
            String message,
            String details) {
        ProductNotification notification = ProductNotification.builder()
                .product(product)
                .type("PRICE_VIOLATION")
                .title("Giá bán vi phạm quy tắc")
                .message(message)
                .details(details)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Create a stock warning notification
     */
    public ProductNotification createStockWarningNotification(
            Product product,
            String message) {
        ProductNotification notification = ProductNotification.builder()
                .product(product)
                .type("STOCK_WARNING")
                .title("Cảnh báo tồn kho")
                .message(message)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get all unread notifications
     */
    public List<ProductNotification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedDateDesc();
    }

    /**
     * Get unread notifications for a product
     */
    public List<ProductNotification> getUnreadNotificationsForProduct(Integer productId) {
        return notificationRepository.findByProduct_ProductIdAndIsReadFalseOrderByCreatedDateDesc(productId);
    }

    /**
     * Get all notifications for a product
     */
    public List<ProductNotification> getNotificationsForProduct(Integer productId) {
        return notificationRepository.findByProduct_ProductIdOrderByCreatedDateDesc(productId);
    }

    /**
     * Mark notification as read
     */
    public ProductNotification markAsRead(Integer notificationId) {
        ProductNotification notification = notificationRepository.findById(notificationId)
                .orElse(null);

        if (notification != null) {
            notification.setIsRead(true);
            notification.setReadDate(LocalDateTime.now());
            return notificationRepository.save(notification);
        }

        return null;
    }

    /**
     * Mark all notifications as read
     */
    public void markAllAsRead() {
        List<ProductNotification> unreadNotifications = getUnreadNotifications();

        for (ProductNotification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadDate(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Delete all notifications for a product
     */
    public void deleteNotificationsForProduct(Integer productId) {
        List<ProductNotification> notifications = notificationRepository
                .findByProduct_ProductIdOrderByCreatedDateDesc(productId);

        notificationRepository.deleteAll(notifications);
    }

    /**
     * Count unread notifications
     */
    public long countUnreadNotifications() {
        return notificationRepository.countUnreadNotifications();
    }

    /**
     * Get recent notifications (last 24 hours)
     */
    public List<ProductNotification> getRecentNotifications() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return notificationRepository.findRecentNotifications(since);
    }

    /**
     * Create a notification for product action (add, update, delete)
     */
    public ProductNotification createProductActionNotification(
            Product product,
            String action,
            String details) {
        String title = "";
        String message = "";

        switch (action) {
            case "ADD_PRODUCT":
                title = "Sản phẩm mới đã được thêm";
                message = "Sản phẩm '" + product.getTitle() + "' đã được thêm vào hệ thống";
                break;
            case "UPDATE_PRODUCT":
                title = "Sản phẩm đã được cập nhật";
                message = "Sản phẩm '" + product.getTitle() + "' đã được cập nhật";
                break;
            case "DELETE_PRODUCT":
                title = "Sản phẩm đã bị xóa";
                message = "Sản phẩm '" + product.getTitle() + "' đã bị xóa khỏi hệ thống";
                break;
            case "DEACTIVATE_PRODUCT":
                title = "Sản phẩm đã được vô hiệu hóa";
                message = "Sản phẩm '" + product.getTitle() + "' đã được vô hiệu hóa";
                break;
            case "ACTIVATE_PRODUCT":
                title = "Sản phẩm đã được kích hoạt";
                message = "Sản phẩm '" + product.getTitle() + "' đã được kích hoạt lại";
                break;
            default:
                title = "Hành động sản phẩm";
                message = "Sản phẩm '" + product.getTitle() + "' có hành động: " + action;
        }

        ProductNotification notification = ProductNotification.builder()
                .product(product)
                .type("PRODUCT_ACTION")
                .title(title)
                .message(message)
                .details(details)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }
}
