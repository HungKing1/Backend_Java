package com.controller;

import com.entity.DVD;
import com.entity.ProductHistory;
import com.repository.ProductHistoryRepository;
import com.service.DVDService;
import com.service.ProductNotificationService;
import com.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dvds")
public class DVDController {
    @Autowired
    private DVDService dvdService;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductNotificationService notificationService;

    @Autowired
    private ProductService productService;

    /**
     * Create a deep copy of DVD to preserve old values
     */
    private DVD copyDVD(DVD dvd) {
        if (dvd == null)
            return null;
        DVD copy = new DVD();
        copy.setProductId(dvd.getProductId());
        copy.setBarcode(dvd.getBarcode());
        copy.setTitle(dvd.getTitle());
        copy.setCategory(dvd.getCategory());
        copy.setDescription(dvd.getDescription());
        copy.setImageUrl(dvd.getImageUrl());
        copy.setDimensions(dvd.getDimensions());
        copy.setWeight(dvd.getWeight());
        copy.setOriginalPrice(dvd.getOriginalPrice());
        copy.setCurrentPrice(dvd.getCurrentPrice());
        copy.setStockQuantity(dvd.getStockQuantity());
        copy.setStatus(dvd.getStatus());
        copy.setIsActive(dvd.getIsActive());
        copy.setDirector(dvd.getDirector());
        copy.setStudio(dvd.getStudio());
        copy.setRuntime(dvd.getRuntime());
        copy.setLanguage(dvd.getLanguage());
        copy.setSubtitles(dvd.getSubtitles());
        copy.setDiscType(dvd.getDiscType());
        return copy;
    }

    /**
     * Get only changed fields between old and new DVD
     */
    private String getChangedFields(DVD oldDvd, DVD newDvd) {
        StringBuilder changes = new StringBuilder();

        // Product base fields
        if (!equalsNullable(oldDvd.getBarcode(), newDvd.getBarcode())) {
            changes.append("Mã vạch: ").append(nullable(oldDvd.getBarcode())).append(" → ")
                    .append(nullable(newDvd.getBarcode())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getTitle(), newDvd.getTitle())) {
            changes.append("Tiêu đề: ").append(nullable(oldDvd.getTitle())).append(" → ")
                    .append(nullable(newDvd.getTitle())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getCategory(), newDvd.getCategory())) {
            changes.append("Danh mục: ").append(nullable(oldDvd.getCategory())).append(" → ")
                    .append(nullable(newDvd.getCategory())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getDescription(), newDvd.getDescription())) {
            changes.append("Mô tả: ").append(nullable(oldDvd.getDescription())).append(" → ")
                    .append(nullable(newDvd.getDescription())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getImageUrl(), newDvd.getImageUrl())) {
            changes.append("URL hình ảnh: ").append(nullable(oldDvd.getImageUrl())).append(" → ")
                    .append(nullable(newDvd.getImageUrl())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getDimensions(), newDvd.getDimensions())) {
            changes.append("Kích thước: ").append(nullable(oldDvd.getDimensions())).append(" → ")
                    .append(nullable(newDvd.getDimensions())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getWeight(), newDvd.getWeight())) {
            changes.append("Trọng lượng: ").append(nullable(oldDvd.getWeight())).append(" → ")
                    .append(nullable(newDvd.getWeight())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getOriginalPrice(), newDvd.getOriginalPrice())) {
            changes.append("Giá gốc: ").append(String.format("%.0f", oldDvd.getOriginalPrice())).append("đ → ")
                    .append(String.format("%.0f", newDvd.getOriginalPrice())).append("đ | ");
        }
        if (!equalsNullable(oldDvd.getCurrentPrice(), newDvd.getCurrentPrice())) {
            changes.append("Giá bán: ").append(String.format("%.0f", oldDvd.getCurrentPrice())).append("đ → ")
                    .append(String.format("%.0f", newDvd.getCurrentPrice())).append("đ | ");
        }
        if (!equalsNullable(oldDvd.getStockQuantity(), newDvd.getStockQuantity())) {
            changes.append("Số lượng: ").append(nullable(oldDvd.getStockQuantity())).append(" → ")
                    .append(nullable(newDvd.getStockQuantity())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getStatus(), newDvd.getStatus())) {
            changes.append("Trạng thái: ").append(nullable(oldDvd.getStatus())).append(" → ")
                    .append(nullable(newDvd.getStatus())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getIsActive(), newDvd.getIsActive())) {
            changes.append("Hoạt động: ").append(nullable(oldDvd.getIsActive())).append(" → ")
                    .append(nullable(newDvd.getIsActive())).append(" | ");
        }

        // DVD specific fields
        if (!equalsNullable(oldDvd.getDirector(), newDvd.getDirector())) {
            changes.append("Đạo diễn: ").append(nullable(oldDvd.getDirector())).append(" → ")
                    .append(nullable(newDvd.getDirector())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getStudio(), newDvd.getStudio())) {
            changes.append("Studio: ").append(nullable(oldDvd.getStudio())).append(" → ")
                    .append(nullable(newDvd.getStudio())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getRuntime(), newDvd.getRuntime())) {
            changes.append("Thời lượng: ").append(nullable(oldDvd.getRuntime())).append(" → ")
                    .append(nullable(newDvd.getRuntime())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getLanguage(), newDvd.getLanguage())) {
            changes.append("Ngôn ngữ: ").append(nullable(oldDvd.getLanguage())).append(" → ")
                    .append(nullable(newDvd.getLanguage())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getSubtitles(), newDvd.getSubtitles())) {
            changes.append("Phụ đề: ").append(nullable(oldDvd.getSubtitles())).append(" → ")
                    .append(nullable(newDvd.getSubtitles())).append(" | ");
        }
        if (!equalsNullable(oldDvd.getDiscType(), newDvd.getDiscType())) {
            changes.append("Loại đĩa: ").append(nullable(oldDvd.getDiscType())).append(" → ")
                    .append(nullable(newDvd.getDiscType())).append(" | ");
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
    public List<DVD> getAllDVDs() {
        return dvdService.findAll();
    }

    @GetMapping("/{id}")
    public DVD getDVDById(@PathVariable Integer id) {
        return dvdService.findByProductId(id);
    }

    @PostMapping
    public ResponseEntity<?> createDVD(@RequestBody DVD dvd) {
        try {
            productService.validatePriceConstraint(dvd.getOriginalPrice(), dvd.getCurrentPrice());
            if (dvd.getCategory() == null || dvd.getCategory().isEmpty()) {
                dvd.setCategory("DVD");
            }
            DVD savedDvd = dvdService.save(dvd);
            Integer changedBy = 1;
            ProductHistory history = ProductHistory.builder()
                    .product(savedDvd)
                    .action("ADD_PRODUCT")
                    .oldValue("Thêm sản phẩm DVD")
                    .newValue("Thêm sản phẩm DVD")
                    .changedBy(changedBy)
                    .changedDate(java.time.LocalDateTime.now())
                    .reason("Thêm DVD mới: " + savedDvd.getTitle())
                    .build();
            productHistoryRepository.save(history);
            notificationService.createProductActionNotification(
                    savedDvd,
                    "ADD_PRODUCT",
                    "DVD '" + savedDvd.getTitle() + "' vừa được thêm vào hệ thống");
            return ResponseEntity.ok(savedDvd);
        } catch (IllegalArgumentException e) {
            // Create notification for validation error
            notificationService.createInvalidInputNotification(
                    null,
                    "Lỗi tạo sản phẩm DVD",
                    e.getMessage(),
                    "Dữ liệu nhập vào không hợp lệ khi tạo DVD mới");
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDVD(@PathVariable Integer id, @RequestBody DVD dvd) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(dvd.getOriginalPrice(), dvd.getCurrentPrice());

            dvd.setProductId(id);
            // Ensure category is set to "DVD"
            if (dvd.getCategory() == null || dvd.getCategory().isEmpty()) {
                dvd.setCategory("DVD");
            }

            // Get adjustment reason from request if available
            String adjustmentReason = dvd.getReasonForAdjustment();
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token in real scenario

            // Validate adjustment reason when stock quantity changes
            DVD oldDvd = dvdService.findByProductId(id);
            // Create a copy of old DVD before saving to preserve old values
            DVD oldDvdCopy = copyDVD(oldDvd);

            if (oldDvd != null && !oldDvd.getStockQuantity().equals(dvd.getStockQuantity())) {
                if (adjustmentReason == null || adjustmentReason.trim().isEmpty()) {
                    // Create notification for missing adjustment reason
                    notificationService.createInvalidInputNotification(
                            oldDvd,
                            "Lý do chỉnh sửa tồn kho bị thiếu",
                            "Sản phẩm DVD " + oldDvd.getTitle() + " được cập nhật số lượng nhưng không có lý do",
                            "Hãy cung cấp lý do khi thay đổi số lượng tồn kho");
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Vui lòng cung cấp lý do khi thay đổi số lượng tồn kho"));
                }
            }

            // Save with history tracking
            DVD savedDvd = dvdService.saveWithHistory(dvd, adjustmentReason, changedBy);

            // Log detailed update in product history
            if (oldDvdCopy != null) {
                String changedFieldsInfo = getChangedFields(oldDvdCopy, savedDvd);
                ProductHistory history = ProductHistory.builder()
                        .product(savedDvd)
                        .action("UPDATE_PRODUCT")
                        .oldValue(changedFieldsInfo.length() > 0 ? changedFieldsInfo : "Không có thay đổi")
                        .newValue("Cập nhật DVD")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason(adjustmentReason != null ? adjustmentReason : "Cập nhật thông tin DVD")
                        .build();
                productHistoryRepository.save(history);
            }

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedDvd,
                    "UPDATE_PRODUCT",
                    "DVD '" + savedDvd.getTitle() + "' vừa được cập nhật. " +
                            (adjustmentReason != null ? "Lý do: " + adjustmentReason : ""));

            return ResponseEntity.ok(savedDvd);
        } catch (IllegalArgumentException e) {
            // Create notification for price violation
            DVD existingDvd = dvdService.findByProductId(id);
            if (existingDvd != null) {
                notificationService.createPriceViolationNotification(
                        existingDvd,
                        e.getMessage(),
                        "Giá bán phải nằm trong khoảng 30%-150% của giá gốc");
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDVD(@PathVariable Integer id) {
        try {
            DVD dvd = dvdService.findByProductId(id);
            if (dvd != null) {
                Integer changedBy = 1;
                ProductHistory history = ProductHistory.builder()
                        .product(dvd)
                        .action("DELETE_PRODUCT")
                        .oldValue("Xóa sản phẩm DVD")
                        .newValue("Xóa sản phẩm DVD")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason("Xóa DVD: " + dvd.getTitle())
                        .build();
                productHistoryRepository.save(history);

                // Create product action notification
                notificationService.createProductActionNotification(
                        dvd,
                        "DELETE_PRODUCT",
                        "DVD '" + dvd.getTitle() + "' vừa bị xóa khỏi hệ thống");
            }
            dvdService.deleteByProductId(id);
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
