package com.controller;

import com.entity.Newspaper;
import com.entity.ProductHistory;
import com.repository.ProductHistoryRepository;
import com.service.NewspaperService;
import com.service.ProductNotificationService;
import com.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newspapers")
public class NewspaperController {
    @Autowired
    private NewspaperService newspaperService;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductNotificationService notificationService;

    @Autowired
    private ProductService productService;

    /**
     * Create a deep copy of Newspaper to preserve old values
     */
    private Newspaper copyNewspaper(Newspaper newspaper) {
        if (newspaper == null)
            return null;
        Newspaper copy = new Newspaper();
        copy.setProductId(newspaper.getProductId());
        copy.setBarcode(newspaper.getBarcode());
        copy.setTitle(newspaper.getTitle());
        copy.setCategory(newspaper.getCategory());
        copy.setDescription(newspaper.getDescription());
        copy.setImageUrl(newspaper.getImageUrl());
        copy.setDimensions(newspaper.getDimensions());
        copy.setWeight(newspaper.getWeight());
        copy.setOriginalPrice(newspaper.getOriginalPrice());
        copy.setCurrentPrice(newspaper.getCurrentPrice());
        copy.setStockQuantity(newspaper.getStockQuantity());
        copy.setStatus(newspaper.getStatus());
        copy.setIsActive(newspaper.getIsActive());
        copy.setPublisher(newspaper.getPublisher());
        copy.setEditorInChief(newspaper.getEditorInChief());
        copy.setPublicationDate(newspaper.getPublicationDate());
        copy.setIssueNumber(newspaper.getIssueNumber());
        copy.setFrequency(newspaper.getFrequency());
        return copy;
    }

    /**
     * Get only changed fields between old and new Newspaper
     */
    private String getChangedFields(Newspaper oldNewspaper, Newspaper newNewspaper) {
        StringBuilder changes = new StringBuilder();

        // Product base fields
        if (!equalsNullable(oldNewspaper.getBarcode(), newNewspaper.getBarcode())) {
            changes.append("Mã vạch: ").append(nullable(oldNewspaper.getBarcode())).append(" → ")
                    .append(nullable(newNewspaper.getBarcode())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getTitle(), newNewspaper.getTitle())) {
            changes.append("Tiêu đề: ").append(nullable(oldNewspaper.getTitle())).append(" → ")
                    .append(nullable(newNewspaper.getTitle())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getCategory(), newNewspaper.getCategory())) {
            changes.append("Danh mục: ").append(nullable(oldNewspaper.getCategory())).append(" → ")
                    .append(nullable(newNewspaper.getCategory())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getDescription(), newNewspaper.getDescription())) {
            changes.append("Mô tả: ").append(nullable(oldNewspaper.getDescription())).append(" → ")
                    .append(nullable(newNewspaper.getDescription())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getImageUrl(), newNewspaper.getImageUrl())) {
            changes.append("URL hình ảnh: ").append(nullable(oldNewspaper.getImageUrl())).append(" → ")
                    .append(nullable(newNewspaper.getImageUrl())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getDimensions(), newNewspaper.getDimensions())) {
            changes.append("Kích thước: ").append(nullable(oldNewspaper.getDimensions())).append(" → ")
                    .append(nullable(newNewspaper.getDimensions())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getWeight(), newNewspaper.getWeight())) {
            changes.append("Trọng lượng: ").append(nullable(oldNewspaper.getWeight())).append(" → ")
                    .append(nullable(newNewspaper.getWeight())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getOriginalPrice(), newNewspaper.getOriginalPrice())) {
            changes.append("Giá gốc: ").append(String.format("%.0f", oldNewspaper.getOriginalPrice())).append("đ → ")
                    .append(String.format("%.0f", newNewspaper.getOriginalPrice())).append("đ | ");
        }
        if (!equalsNullable(oldNewspaper.getCurrentPrice(), newNewspaper.getCurrentPrice())) {
            changes.append("Giá bán: ").append(String.format("%.0f", oldNewspaper.getCurrentPrice())).append("đ → ")
                    .append(String.format("%.0f", newNewspaper.getCurrentPrice())).append("đ | ");
        }
        if (!equalsNullable(oldNewspaper.getStockQuantity(), newNewspaper.getStockQuantity())) {
            changes.append("Số lượng: ").append(nullable(oldNewspaper.getStockQuantity())).append(" → ")
                    .append(nullable(newNewspaper.getStockQuantity())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getStatus(), newNewspaper.getStatus())) {
            changes.append("Trạng thái: ").append(nullable(oldNewspaper.getStatus())).append(" → ")
                    .append(nullable(newNewspaper.getStatus())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getIsActive(), newNewspaper.getIsActive())) {
            changes.append("Hoạt động: ").append(nullable(oldNewspaper.getIsActive())).append(" → ")
                    .append(nullable(newNewspaper.getIsActive())).append(" | ");
        }

        // Newspaper specific fields
        if (!equalsNullable(oldNewspaper.getPublisher(), newNewspaper.getPublisher())) {
            changes.append("Nhà xuất bản: ").append(nullable(oldNewspaper.getPublisher())).append(" → ")
                    .append(nullable(newNewspaper.getPublisher())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getEditorInChief(), newNewspaper.getEditorInChief())) {
            changes.append("Tổng biên tập: ").append(nullable(oldNewspaper.getEditorInChief())).append(" → ")
                    .append(nullable(newNewspaper.getEditorInChief())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getPublicationDate(), newNewspaper.getPublicationDate())) {
            changes.append("Ngày phát hành: ").append(nullable(oldNewspaper.getPublicationDate())).append(" → ")
                    .append(nullable(newNewspaper.getPublicationDate())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getIssueNumber(), newNewspaper.getIssueNumber())) {
            changes.append("Số phát hành: ").append(nullable(oldNewspaper.getIssueNumber())).append(" → ")
                    .append(nullable(newNewspaper.getIssueNumber())).append(" | ");
        }
        if (!equalsNullable(oldNewspaper.getFrequency(), newNewspaper.getFrequency())) {
            changes.append("Tần suất: ").append(nullable(oldNewspaper.getFrequency())).append(" → ")
                    .append(nullable(newNewspaper.getFrequency())).append(" | ");
        }

        if (changes.length() > 0) {
            changes.setLength(changes.length() - 3); // Remove trailing " | "
        }
        return changes.toString();
    }

    private boolean equalsNullable(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.equals(b);
    }

    private String nullable(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }

    @GetMapping
    public List<Newspaper> getAllNewspapers() {
        return newspaperService.findAll();
    }

    @GetMapping("/{id}")
    public Newspaper getNewspaperById(@PathVariable Integer id) {
        return newspaperService.findByProductId(id);
    }

    @PostMapping
    public ResponseEntity<?> createNewspaper(@RequestBody Newspaper newspaper) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(newspaper.getOriginalPrice(), newspaper.getCurrentPrice());

            // Ensure category is set to "Newspaper"
            if (newspaper.getCategory() == null || newspaper.getCategory().isEmpty()) {
                newspaper.setCategory("Newspaper");
            }

            // Save Newspaper first to get the ID
            Newspaper savedNewspaper = newspaperService.save(newspaper);

            // Log creation in product history
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token
            ProductHistory history = ProductHistory.builder()
                    .product(savedNewspaper)
                    .action("ADD_PRODUCT")
                    .oldValue("Thêm sản phẩm báo")
                    .newValue("Thêm sản phẩm báo")
                    .changedBy(changedBy)
                    .changedDate(java.time.LocalDateTime.now())
                    .reason("Thêm báo mới: " + savedNewspaper.getTitle())
                    .build();
            productHistoryRepository.save(history);

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedNewspaper,
                    "ADD_PRODUCT",
                    "Báo '" + savedNewspaper.getTitle() + "' vừa được thêm vào hệ thống");

            return ResponseEntity.ok(savedNewspaper);
        } catch (IllegalArgumentException e) {
            // Create notification for validation error
            notificationService.createInvalidInputNotification(
                    null,
                    "Lỗi tạo sản phẩm báo",
                    e.getMessage(),
                    "Dữ liệu nhập vào không hợp lệ khi tạo báo mới");
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNewspaper(@PathVariable Integer id, @RequestBody Newspaper newspaper) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(newspaper.getOriginalPrice(), newspaper.getCurrentPrice());

            newspaper.setProductId(id);
            // Ensure category is set to "Newspaper"
            if (newspaper.getCategory() == null || newspaper.getCategory().isEmpty()) {
                newspaper.setCategory("Newspaper");
            }

            // Get adjustment reason from request if available
            String adjustmentReason = newspaper.getReasonForAdjustment();
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token in real scenario

            // Validate adjustment reason when stock quantity changes
            Newspaper oldNewspaper = newspaperService.findByProductId(id);
            // Create a copy of old Newspaper before saving to preserve old values
            Newspaper oldNewspaperCopy = copyNewspaper(oldNewspaper);

            if (oldNewspaper != null && !oldNewspaper.getStockQuantity().equals(newspaper.getStockQuantity())) {
                if (adjustmentReason == null || adjustmentReason.trim().isEmpty()) {
                    // Create notification for missing adjustment reason
                    notificationService.createInvalidInputNotification(
                            oldNewspaper,
                            "Lý do chỉnh sửa tồn kho bị thiếu",
                            "Báo " + oldNewspaper.getTitle() + " được cập nhật số lượng nhưng không có lý do",
                            "Hãy cung cấp lý do khi thay đổi số lượng tồn kho");
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Vui lòng cung cấp lý do khi thay đổi số lượng tồn kho"));
                }
            }

            // Save with history tracking
            Newspaper savedNewspaper = newspaperService.saveWithHistory(newspaper, adjustmentReason, changedBy);

            // Log detailed update in product history
            if (oldNewspaperCopy != null) {
                String changedFieldsInfo = getChangedFields(oldNewspaperCopy, savedNewspaper);
                ProductHistory history = ProductHistory.builder()
                        .product(savedNewspaper)
                        .action("UPDATE_PRODUCT")
                        .oldValue(changedFieldsInfo.length() > 0 ? changedFieldsInfo : "Không có thay đổi")
                        .newValue("Cập nhật báo")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason(adjustmentReason != null ? adjustmentReason : "Cập nhật thông tin báo")
                        .build();
                productHistoryRepository.save(history);
            }

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedNewspaper,
                    "UPDATE_PRODUCT",
                    "Báo '" + savedNewspaper.getTitle() + "' vừa được cập nhật. " +
                            (adjustmentReason != null ? "Lý do: " + adjustmentReason : ""));

            return ResponseEntity.ok(savedNewspaper);
        } catch (IllegalArgumentException e) {
            // Create notification for price violation
            Newspaper existingNewspaper = newspaperService.findByProductId(id);
            if (existingNewspaper != null) {
                notificationService.createPriceViolationNotification(
                        existingNewspaper,
                        e.getMessage(),
                        "Giá bán phải nằm trong khoảng 30%-150% của giá gốc");
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNewspaper(@PathVariable Integer id) {
        try {
            Newspaper newspaper = newspaperService.findByProductId(id);
            if (newspaper != null) {
                Integer changedBy = 1;
                ProductHistory history = ProductHistory.builder()
                        .product(newspaper)
                        .action("DELETE_PRODUCT")
                        .oldValue("Xóa sản phẩm báo")
                        .newValue("Xóa sản phẩm báo")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason("Xóa báo: " + newspaper.getTitle())
                        .build();
                productHistoryRepository.save(history);

                // Create product action notification
                notificationService.createProductActionNotification(
                        newspaper,
                        "DELETE_PRODUCT",
                        "Báo '" + newspaper.getTitle() + "' vừa bị xóa khỏi hệ thống");
            }
            newspaperService.deleteByProductId(id);
            return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi khi xóa: " + e.getMessage()));
        }
    }

    /**
     * Get product adjustment history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getProductHistory(@PathVariable Integer id) {
        try {
            List<ProductHistory> history = productHistoryRepository.findByProduct_ProductId(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch sử sản phẩm: " + e.getMessage()));
        }
    }
}
