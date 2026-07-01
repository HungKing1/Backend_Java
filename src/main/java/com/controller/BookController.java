package com.controller;

import com.entity.Book;
import com.entity.ProductHistory;
import com.repository.ProductHistoryRepository;
import com.service.BookService;
import com.service.ProductNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.service.ProductService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductNotificationService notificationService;

    @Autowired
    private ProductService productService;

    /**
     * Create a deep copy of Book to preserve old values
     */
    private Book copyBook(Book book) {
        if (book == null)
            return null;
        Book copy = new Book();
        copy.setProductId(book.getProductId());
        copy.setBarcode(book.getBarcode());
        copy.setTitle(book.getTitle());
        copy.setCategory(book.getCategory());
        copy.setDescription(book.getDescription());
        copy.setImageUrl(book.getImageUrl());
        copy.setDimensions(book.getDimensions());
        copy.setWeight(book.getWeight());
        copy.setOriginalPrice(book.getOriginalPrice());
        copy.setCurrentPrice(book.getCurrentPrice());
        copy.setStockQuantity(book.getStockQuantity());
        copy.setStatus(book.getStatus());
        copy.setIsActive(book.getIsActive());
        copy.setAuthor(book.getAuthor());
        copy.setPublisher(book.getPublisher());
        copy.setPublicationDate(book.getPublicationDate());
        copy.setCoverType(book.getCoverType());
        copy.setNumOfPages(book.getNumOfPages());
        copy.setLanguage(book.getLanguage());
        copy.setGenre(book.getGenre());
        return copy;
    }

    /**
     * Get only changed fields between old and new Book
     */
    private String getChangedFields(Book oldBook, Book newBook) {
        StringBuilder changes = new StringBuilder();

        // Product base fields
        if (!equalsNullable(oldBook.getBarcode(), newBook.getBarcode())) {
            changes.append("Mã vạch: ").append(nullable(oldBook.getBarcode())).append(" → ")
                    .append(nullable(newBook.getBarcode())).append(" | ");
        }
        if (!equalsNullable(oldBook.getTitle(), newBook.getTitle())) {
            changes.append("Tiêu đề: ").append(nullable(oldBook.getTitle())).append(" → ")
                    .append(nullable(newBook.getTitle())).append(" | ");
        }
        if (!equalsNullable(oldBook.getCategory(), newBook.getCategory())) {
            changes.append("Danh mục: ").append(nullable(oldBook.getCategory())).append(" → ")
                    .append(nullable(newBook.getCategory())).append(" | ");
        }
        if (!equalsNullable(oldBook.getDescription(), newBook.getDescription())) {
            changes.append("Mô tả: ").append(nullable(oldBook.getDescription())).append(" → ")
                    .append(nullable(newBook.getDescription())).append(" | ");
        }
        if (!equalsNullable(oldBook.getImageUrl(), newBook.getImageUrl())) {
            changes.append("URL hình ảnh: ").append(nullable(oldBook.getImageUrl())).append(" → ")
                    .append(nullable(newBook.getImageUrl())).append(" | ");
        }
        if (!equalsNullable(oldBook.getDimensions(), newBook.getDimensions())) {
            changes.append("Kích thước: ").append(nullable(oldBook.getDimensions())).append(" → ")
                    .append(nullable(newBook.getDimensions())).append(" | ");
        }
        if (!equalsNullable(oldBook.getWeight(), newBook.getWeight())) {
            changes.append("Trọng lượng: ").append(nullable(oldBook.getWeight())).append(" → ")
                    .append(nullable(newBook.getWeight())).append(" | ");
        }
        if (!equalsNullable(oldBook.getOriginalPrice(), newBook.getOriginalPrice())) {
            changes.append("Giá gốc: ").append(String.format("%.0f", oldBook.getOriginalPrice())).append("đ → ")
                    .append(String.format("%.0f", newBook.getOriginalPrice())).append("đ | ");
        }
        if (!equalsNullable(oldBook.getCurrentPrice(), newBook.getCurrentPrice())) {
            changes.append("Giá bán: ").append(String.format("%.0f", oldBook.getCurrentPrice())).append("đ → ")
                    .append(String.format("%.0f", newBook.getCurrentPrice())).append("đ | ");
        }
        if (!equalsNullable(oldBook.getStockQuantity(), newBook.getStockQuantity())) {
            changes.append("Số lượng: ").append(nullable(oldBook.getStockQuantity())).append(" → ")
                    .append(nullable(newBook.getStockQuantity())).append(" | ");
        }
        if (!equalsNullable(oldBook.getStatus(), newBook.getStatus())) {
            changes.append("Trạng thái: ").append(nullable(oldBook.getStatus())).append(" → ")
                    .append(nullable(newBook.getStatus())).append(" | ");
        }
        if (!equalsNullable(oldBook.getIsActive(), newBook.getIsActive())) {
            changes.append("Hoạt động: ").append(nullable(oldBook.getIsActive())).append(" → ")
                    .append(nullable(newBook.getIsActive())).append(" | ");
        }

        // Book specific fields
        if (!equalsNullable(oldBook.getAuthor(), newBook.getAuthor())) {
            changes.append("Tác giả: ").append(nullable(oldBook.getAuthor())).append(" → ")
                    .append(nullable(newBook.getAuthor())).append(" | ");
        }
        if (!equalsNullable(oldBook.getPublisher(), newBook.getPublisher())) {
            changes.append("Nhà xuất bản: ").append(nullable(oldBook.getPublisher())).append(" → ")
                    .append(nullable(newBook.getPublisher())).append(" | ");
        }
        if (!equalsNullable(oldBook.getPublicationDate(), newBook.getPublicationDate())) {
            changes.append("Ngày xuất bản: ").append(nullable(oldBook.getPublicationDate())).append(" → ")
                    .append(nullable(newBook.getPublicationDate())).append(" | ");
        }
        if (!equalsNullable(oldBook.getCoverType(), newBook.getCoverType())) {
            changes.append("Loại bìa: ").append(nullable(oldBook.getCoverType())).append(" → ")
                    .append(nullable(newBook.getCoverType())).append(" | ");
        }
        if (!equalsNullable(oldBook.getNumOfPages(), newBook.getNumOfPages())) {
            changes.append("Số trang: ").append(nullable(oldBook.getNumOfPages())).append(" → ")
                    .append(nullable(newBook.getNumOfPages())).append(" | ");
        }
        if (!equalsNullable(oldBook.getLanguage(), newBook.getLanguage())) {
            changes.append("Ngôn ngữ: ").append(nullable(oldBook.getLanguage())).append(" → ")
                    .append(nullable(newBook.getLanguage())).append(" | ");
        }
        if (!equalsNullable(oldBook.getGenre(), newBook.getGenre())) {
            changes.append("Thể loại: ").append(nullable(oldBook.getGenre())).append(" → ")
                    .append(nullable(newBook.getGenre())).append(" | ");
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
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Integer id) {
        return bookService.findByProductId(id);
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody Book book) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(book.getOriginalPrice(), book.getCurrentPrice());

            // Ensure category is set to "Book"
            if (book.getCategory() == null || book.getCategory().isEmpty()) {
                book.setCategory("Book");
            }

            // Save book first to get the ID
            Book savedBook = bookService.save(book);

            // Log creation in product history
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token
            ProductHistory history = ProductHistory.builder()
                    .product(savedBook)
                    .action("ADD_PRODUCT")
                    .oldValue("Thêm sản phẩm sách")
                    .newValue("Thêm sản phẩm sách")
                    .changedBy(changedBy)
                    .changedDate(java.time.LocalDateTime.now())
                    .reason("Thêm sách mới: " + savedBook.getTitle())
                    .build();
            productHistoryRepository.save(history);

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedBook,
                    "ADD_PRODUCT",
                    "Sách '" + savedBook.getTitle() + "' vừa được thêm vào hệ thống");

            return ResponseEntity.ok(savedBook);
        } catch (IllegalArgumentException e) {
            // Create notification for validation error
            notificationService.createInvalidInputNotification(
                    null,
                    "Lỗi tạo sản phẩm sách",
                    e.getMessage(),
                    "Dữ liệu nhập vào không hợp lệ khi tạo sách mới");
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Integer id, @RequestBody Book book) {
        try {
            // Validate price constraint
            productService.validatePriceConstraint(book.getOriginalPrice(), book.getCurrentPrice());

            book.setProductId(id);
            // Ensure category is set to "Book"
            if (book.getCategory() == null || book.getCategory().isEmpty()) {
                book.setCategory("Book");
            }

            // Get adjustment reason from request if available
            String reasonForAdjustment = book.getReasonForAdjustment();
            Integer changedBy = 1; // Default user ID, should be fetched from JWT token in real scenario

            // Validate adjustment reason when stock quantity changes
            Book oldBook = bookService.findByProductId(id);
            // Create a copy of old Book before saving to preserve old values
            Book oldBookCopy = copyBook(oldBook);

            if (oldBook != null && !oldBook.getStockQuantity().equals(book.getStockQuantity())) {
                if (reasonForAdjustment == null || reasonForAdjustment.trim().isEmpty()) {
                    // Create notification for missing adjustment reason
                    notificationService.createInvalidInputNotification(
                            oldBook,
                            "Lý do chỉnh sửa tồn kho bị thiếu",
                            "Sách " + oldBook.getTitle() + " được cập nhật số lượng nhưng không có lý do",
                            "Hãy cung cấp lý do khi thay đổi số lượng tồn kho");
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Vui lòng cung cấp lý do khi thay đổi số lượng tồn kho"));
                }
            }

            // Save with history tracking
            Book savedBook = bookService.saveWithHistory(book, reasonForAdjustment, changedBy);

            // Log detailed update in product history
            if (oldBookCopy != null) {
                String changedFieldsInfo = getChangedFields(oldBookCopy, savedBook);
                ProductHistory history = ProductHistory.builder()
                        .product(savedBook)
                        .action("UPDATE_PRODUCT")
                        .oldValue(changedFieldsInfo.length() > 0 ? changedFieldsInfo : "Không có thay đổi")
                        .newValue("Cập nhật sách")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason(reasonForAdjustment != null ? reasonForAdjustment : "Cập nhật thông tin sách")
                        .build();
                productHistoryRepository.save(history);
            }

            // Create product action notification
            notificationService.createProductActionNotification(
                    savedBook,
                    "UPDATE_PRODUCT",
                    "Sách '" + savedBook.getTitle() + "' vừa được cập nhật. " +
                            (reasonForAdjustment != null ? "Lý do: " + reasonForAdjustment : ""));

            return ResponseEntity.ok(savedBook);
        } catch (IllegalArgumentException e) {
            // Create notification for price violation
            Book existingBook = bookService.findByProductId(id);
            if (existingBook != null) {
                notificationService.createPriceViolationNotification(
                        existingBook,
                        e.getMessage(),
                        "Giá bán phải nằm trong khoảng 30%-150% của giá gốc");
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {
        try {
            Book book = bookService.findByProductId(id);
            if (book != null) {
                Integer changedBy = 1;
                ProductHistory history = ProductHistory.builder()
                        .product(book)
                        .action("DELETE_PRODUCT")
                        .oldValue("Xóa sản phẩm sách")
                        .newValue("Xóa sản phẩm sách")
                        .changedBy(changedBy)
                        .changedDate(java.time.LocalDateTime.now())
                        .reason("Xóa sách: " + book.getTitle())
                        .build();
                productHistoryRepository.save(history);

                // Create product action notification
                notificationService.createProductActionNotification(
                        book,
                        "DELETE_PRODUCT",
                        "Sách '" + book.getTitle() + "' vừa bị xóa khỏi hệ thống");
            }
            bookService.deleteByProductId(id);
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
