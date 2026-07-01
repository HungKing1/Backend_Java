package com.controller;

import com.entity.ProductNotification;
import com.service.ProductNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class ProductNotificationController {

    @Autowired
    private ProductNotificationService notificationService;

    /**
     * Get all unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            List<ProductNotification> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi lấy thông báo: " + e.getMessage()));
        }
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            long count = notificationService.countUnreadNotifications();
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi lấy số thông báo chưa đọc: " + e.getMessage()));
        }
    }

    /**
     * Get all notifications for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductNotifications(@PathVariable Integer productId) {
        try {
            List<ProductNotification> notifications = notificationService.getNotificationsForProduct(productId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi lấy thông báo sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Get recent notifications (last 24 hours)
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications() {
        try {
            List<ProductNotification> notifications = notificationService.getRecentNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi lấy thông báo gần đây: " + e.getMessage()));
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer notificationId) {
        try {
            ProductNotification notification = notificationService.markAsRead(notificationId);
            if (notification != null) {
                return ResponseEntity.ok(notification);
            } else {
                return ResponseEntity.status(404)
                        .body(Map.of("message", "Thông báo không tồn tại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi cập nhật thông báo: " + e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả thông báo đã đọc"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi cập nhật thông báo: " + e.getMessage()));
        }
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of("message", "Thông báo đã được xóa"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi xóa thông báo: " + e.getMessage()));
        }
    }

    /**
     * Delete all notifications for a product
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> deleteProductNotifications(@PathVariable Integer productId) {
        try {
            notificationService.deleteNotificationsForProduct(productId);
            return ResponseEntity.ok(Map.of("message", "Tất cả thông báo của sản phẩm đã được xóa"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Lỗi khi xóa thông báo: " + e.getMessage()));
        }
    }
}
