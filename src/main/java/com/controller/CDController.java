package com.controller;

import com.entity.CD;
import com.entity.ProductHistory;
import com.repository.ProductHistoryRepository;
import com.service.CDService;
import com.service.ProductNotificationService;
import com.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cds")
public class CDController {
    @Autowired
    private CDService cdService;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductNotificationService notificationService;

    @Autowired
    private ProductService productService;

    /**
     * Create a deep copy of CD to preserve old values
     */
    private CD copyCD(CD cd) {
        if (cd == null)
            return null;
        CD copy = new CD();
        copy.setProductId(cd.getProductId());
        copy.setBarcode(cd.getBarcode());
        copy.setTitle(cd.getTitle());
        copy.setCategory(cd.getCategory());
        copy.setDescription(cd.getDescription());
        copy.setImageUrl(cd.getImageUrl());
        copy.setDimensions(cd.getDimensions());
        copy.setWeight(cd.getWeight());
        copy.setOriginalPrice(cd.getOriginalPrice());
        copy.setCurrentPrice(cd.getCurrentPrice());
        copy.setStockQuantity(cd.getStockQuantity());
        copy.setStatus(cd.getStatus());
        copy.setIsActive(cd.getIsActive());
        copy.setArtist(cd.getArtist());
        copy.setGenre(cd.getGenre());
        copy.setRecordLabel(cd.getRecordLabel());
        copy.setStudio(cd.getStudio());
        copy.setTrackList(cd.getTrackList());
        copy.setDiscType(cd.getDiscType());
        copy.setReleaseDate(cd.getReleaseDate());
        return copy;
    }

    /**
     * Get only changed fields between old and new CD
     */
    private String getChangedFields(CD oldCd, CD newCd) {
        StringBuilder changes = new StringBuilder();

        // Product base fields
        if (!equalsNullable(oldCd.getBarcode(), newCd.getBarcode())) {
            changes.append("Mã vạch: ").append(nullable(oldCd.getBarcode())).append(" → ")
                    .append(nullable(newCd.getBarcode())).append(" | ");
        }
        if (!equalsNullable(oldCd.getTitle(), newCd.getTitle())) {
            changes.append("Tiêu đề: ").append(nullable(oldCd.getTitle())).append(" → ")
                    .append(nullable(newCd.getTitle())).append(" | ");
        }
        if (!equalsNullable(oldCd.getCategory(), newCd.getCategory())) {
            changes.append("Danh mục: ").append(nullable(oldCd.getCategory())).append(" → ")
                    .append(nullable(newCd.getCategory())).append(" | ");
        }
        if (!equalsNullable(oldCd.getDescription(), newCd.getDescription())) {
            changes.append("Mô tả: ").append(nullable(oldCd.getDescription())).append(" → ")
                    .append(nullable(newCd.getDescription())).append(" | ");
        }
        if (!equalsNullable(oldCd.getImageUrl(), newCd.getImageUrl())) {
            changes.append("URL hình ảnh: ").append(nullable(oldCd.getImageUrl())).append(" → ")
                    .append(nullable(newCd.getImageUrl())).append(" | ");
        }
        if (!equalsNullable(oldCd.getDimensions(), newCd.getDimensions())) {
            changes.append("Kích thước: ").append(nullable(oldCd.getDimensions())).append(" → ")
                    .append(nullable(newCd.getDimensions())).append(" | ");
        }
        if (!equalsNullable(oldCd.getWeight(), newCd.getWeight())) {
            changes.append("Trọng lượng: ").append(nullable(oldCd.getWeight())).append(" → ")
                    .append(nullable(newCd.getWeight())).append(" | ");
        }
        if (!equalsNullable(oldCd.getOriginalPrice(), newCd.getOriginalPrice())) {
            changes.append("Giá gốc: ").append(String.format("%.0f", oldCd.getOriginalPrice())).append("đ → ")
                    .append(String.format("%.0f", newCd.getOriginalPrice())).append("đ | ");
        }
        if (!equalsNullable(oldCd.getCurrentPrice(), newCd.getCurrentPrice())) {
            changes.append("Giá bán: ").append(String.format("%.0f", oldCd.getCurrentPrice())).append("đ → ")
                    .append(String.format("%.0f", newCd.getCurrentPrice())).append("đ | ");
        }
        if (!equalsNullable(oldCd.getStockQuantity(), newCd.getStockQuantity())) {
            changes.append("Số lượng: ").append(nullable(oldCd.getStockQuantity())).append(" → ")
                    .append(nullable(newCd.getStockQuantity())).append(" | ");
        }
        if (!equalsNullable(oldCd.getStatus(), newCd.getStatus())) {
            changes.append("Trạng thái: ").append(nullable(oldCd.getStatus())).append(" → ")
                    .append(nullable(newCd.getStatus())).append(" | ");
        }
        if (!equalsNullable(oldCd.getIsActive(), newCd.getIsActive())) {
            changes.append("Hoạt động: ").append(nullable(oldCd.getIsActive())).append(" → ")
                    .append(nullable(newCd.getIsActive())).append(" | ");
        }

        // CD specific fields
        if (!equalsNullable(oldCd.getArtist(), newCd.getArtist())) {
            changes.append("Nghệ sĩ: ").append(nullable(oldCd.getArtist())).append(" → ")
                    .append(nullable(newCd.getArtist())).append(" | ");
        }
        if (!equalsNullable(oldCd.getGenre(), newCd.getGenre())) {
            changes.append("Thể loại: ").append(nullable(oldCd.getGenre())).append(" → ")
                    .append(nullable(newCd.getGenre())).append(" | ");
        }
        if (!equalsNullable(oldCd.getRecordLabel(), newCd.getRecordLabel())) {
            changes.append("Hãng đĩa: ").append(nullable(oldCd.getRecordLabel())).append(" → ")
                    .append(nullable(newCd.getRecordLabel())).append(" | ");
        }
        if (!equalsNullable(oldCd.getStudio(), newCd.getStudio())) {
            changes.append("Studio: ").append(nullable(oldCd.getStudio())).append(" → ")
                    .append(nullable(newCd.getStudio())).append(" | ");
        }
        if (!equalsNullable(oldCd.getTrackList(), newCd.getTrackList())) {
            changes.append("Danh sách bài hát: ").append(nullable(oldCd.getTrackList())).append(" → ")
                    .append(nullable(newCd.getTrackList())).append(" | ");
        }
        if (!equalsNullable(oldCd.getDiscType(), newCd.getDiscType())) {
            changes.append("Loại đĩa: ").append(nullable(oldCd.getDiscType())).append(" → ")
                    .append(nullable(newCd.getDiscType())).append(" | ");
        }
        if (!equalsNullable(oldCd.getReleaseDate(), newCd.getReleaseDate())) {
            changes.append("Ngày phát hành: ").append(nullable(oldCd.getReleaseDate())).append(" → ")
                    .append(nullable(newCd.getReleaseDate())).append(" | ");
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
    public List<CD> getAllCDs() {
        return cdService.findAll();
    }

    @GetMapping("/{id}")
    public CD getCDById(@PathVariable Integer id) {
        return cdService.findByProductId(id);
    }

    @PostMapping
    public ResponseEntity<?> createCD(@RequestBody CD cd) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(cd.getOriginalPrice(), cd.getCurrentPrice());

            // Ensure category is set to "CD"
            if (cd.getCategory() == null || cd.getCategory().isEmpty()) {
                cd.setCategory("CD");
            }

            // Save CD first to get the ID
            CD savedCd = cdService.save(cd);

            // Log creation in product history
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token
            ProductHistory history = ProductHistory.builder()
                    .product(savedCd)
                    .action("ADD_PRODUCT")
                    .oldValue("Thêm sản phẩm CD")
                    .newValue("Thêm sản phẩm CD")
                    .changedBy(changedBy)
                    .changedDate(java.time.LocalDateTime.now())
                    .reason("Thêm CD mới: " + savedCd.getTitle())
                    .build();
            productHistoryRepository.save(history);

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedCd,
                    "ADD_PRODUCT",
                    "CD '" + savedCd.getTitle() + "' vừa được thêm vào hệ thống");

            return ResponseEntity.ok(savedCd);
        } catch (IllegalArgumentException e) {
            // Create notification for validation error
            notificationService.createInvalidInputNotification(
                    null,
                    "Lỗi tạo sản phẩm CD",
                    e.getMessage(),
                    "Dữ liệu nhập vào không hợp lệ khi tạo CD mới");
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCD(@PathVariable Integer id, @RequestBody CD cd) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(cd.getOriginalPrice(), cd.getCurrentPrice());

            cd.setProductId(id);
            // Ensure category is set to "CD"
            if (cd.getCategory() == null || cd.getCategory().isEmpty()) {
                cd.setCategory("CD");
            }

            // Get adjustment reason from request if available
            String adjustmentReason = cd.getReasonForAdjustment();
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token in real scenario

            // Validate adjustment reason when stock quantity changes
            CD oldCd = cdService.findByProductId(id);
            // Create a copy of old CD before saving to preserve old values
            CD oldCdCopy = copyCD(oldCd);

            if (oldCd != null && !oldCd.getStockQuantity().equals(cd.getStockQuantity())) {
                if (adjustmentReason == null || adjustmentReason.trim().isEmpty()) {
                    // Create notification for missing adjustment reason
                    notificationService.createInvalidInputNotification(
                            oldCd,
                            "Lý do chỉnh sửa tồn kho bị thiếu",
                            "Sản phẩm CD " + oldCd.getTitle() + " được cập nhật số lượng nhưng không có lý do",
                            "Hãy cung cấp lý do khi thay đổi số lượng tồn kho");
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Vui lòng cung cấp lý do khi thay đổi số lượng tồn kho"));
                }
            }

            // Save with history tracking
            CD savedCd = cdService.saveWithHistory(cd, adjustmentReason, changedBy);

            // Log detailed update in product history
            if (oldCdCopy != null) {
                String changedFieldsInfo = getChangedFields(oldCdCopy, savedCd);
                ProductHistory history = ProductHistory.builder()
                        .product(savedCd)
                        .action("UPDATE_PRODUCT")
                        .oldValue(changedFieldsInfo.length() > 0 ? changedFieldsInfo : "Không có thay đổi")
                        .newValue("Cập nhật CD")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason(adjustmentReason != null ? adjustmentReason : "Cập nhật thông tin CD")
                        .build();
                productHistoryRepository.save(history);
            }

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedCd,
                    "UPDATE_PRODUCT",
                    "CD '" + savedCd.getTitle() + "' vừa được cập nhật. " +
                            (adjustmentReason != null ? "Lý do: " + adjustmentReason : ""));

            return ResponseEntity.ok(savedCd);
        } catch (IllegalArgumentException e) {
            // Create notification for price violation
            CD existingCd = cdService.findByProductId(id);
            if (existingCd != null) {
                notificationService.createPriceViolationNotification(
                        existingCd,
                        e.getMessage(),
                        "Giá bán phải nằm trong khoảng 30%-150% của giá gốc");
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCD(@PathVariable Integer id) {
        try {
            CD cd = cdService.findByProductId(id);
            if (cd != null) {
                Integer changedBy = 1;
                ProductHistory history = ProductHistory.builder()
                        .product(cd)
                        .action("DELETE_PRODUCT")
                        .oldValue("Xóa sản phẩm CD")
                        .newValue("Xóa sản phẩm CD")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason("Xóa CD: " + cd.getTitle())
                        .build();
                productHistoryRepository.save(history);

                // Create product action notification
                notificationService.createProductActionNotification(
                        cd,
                        "DELETE_PRODUCT",
                        "CD '" + cd.getTitle() + "' vừa bị xóa khỏi hệ thống");
            }
            cdService.deleteByProductId(id);
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
