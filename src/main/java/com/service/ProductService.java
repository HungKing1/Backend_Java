/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductRepository, Product, ProductRequest, ProductDTO,
* ProductQuantityCheckRequest, ProductQuantityCheckResponse, SearchFilterRequest
* - Lý do: Class sử dụng ProductRepository để truy xuất/lưu trữ dữ liệu thông qua
* các method với parameters là data objects (entities, DTOs, requests).
* Không chia sẻ data structure phức tạp, chỉ truyền nhận objects cần thiết.
*
* 2. COHESION:
* - Mức độ: Communicational Cohesion
* - Giữa các thành phần: getAllProducts(), getProductById(), getProductBytitle(),
* addProduct(), updateProduct(), deleteProduct(), searchProductsByName(),
* toDTO(), toEntity(), checkProductQuantity(), searchProductsWithFilter()
* - Lý do: Tất cả methods đều làm việc trên cùng một entity (Product) và
* ProductRepository. Các operations bao gồm CRUD, conversion (entity<->DTO),
* search và filter đều phục vụ quản lý Product data. Mặc dù có nhiều chức năng
* khác nhau nhưng đều xoay quanh Product entity và data transformation.
* ---------------------------------------------------------
*/

package com.service;

import com.entity.Brand;
import com.entity.Category;
import com.entity.Product;
import com.entity.ProductHistory;
// import com.entity.ProductVariant;
import com.entity.dto.ProductDTO;
import com.entity.dto.ProductVariantDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.repository.BrandRepository;
// import com.repository.CategoryRepository;
import com.repository.ProductRepository;
import com.repository.ProductHistoryRepository;
// import com.repository.ProductVariantRepository;
import com.request.ProductQuantityCheckRequest;
import com.request.ProductRequest;
// import com.request.ProductVariantRequest;
import com.request.SearchFilterRequest;
import com.response.BatchDeleteResponse;
import com.response.ProductDeletionDetail;
import com.response.ProductQuantityCheckResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductHistoryRepository productHistoryRepository;
    // @Autowired
    // private CategoryRepository categoryRepository;
    // @Autowired
    // private BrandRepository brandRepository;
    // @Autowired
    // private ProductVariantRepository productVariantRepository;

    private static final int MAX_BATCH_DELETE = 10; // Max 10 products per batch
    private static final int MAX_DAILY_DELETE = 20; // Max 20 products per day

    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setCurrentPrice(product.getCurrentPrice());
        dto.setWeight(product.getWeight());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getCurrentPrice());
        return dto;
    }

    public Product toEntity(ProductRequest request) {
        // Tạo Product
        Product product = new Product();
        if (request.getProductId() != null) {
            product.setProductId(request.getProductId());
        }
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setWeight(request.getWeight());
        product.setCurrentPrice(request.getCurrentPrice());
        product.setCategory(request.getCategoryName());
        // Resolve category (required) and brand (optional)
        // Integer categoryId = null;
        // if (request.getCategoryName() != null) {
        // categoryId =
        // categoryRepository.findByCategoryNameIgnoreCase(request.getCategoryName())
        // .orElseThrow(() -> new EntityNotFoundException("Category not found"))
        // .getCategoryId();
        // }
        // Integer brandId = null;
        // if (request.getBrandName() != null) {
        // brandId = brandRepository.findByBrandNameIgnoreCase(request.getBrandName())
        // .map(b -> b.getBrandId())
        // .orElse(null);
        // }

        // product.setCategoryId(categoryId);
        // product.setBrandId(brandId);
        // product.setSupportRushOrder(request.getSupportRushOrder());

        // List<ProductVariant> variants = request.getVariants().stream().map(variantReq
        // -> {
        // ProductVariant variant = new ProductVariant();
        // variant.setVariantId(variantReq.getVariantId());
        // variant.setColor(variantReq.getColor());
        // variant.setDiscountPercentage(variantReq.getDiscountPercentage());
        // variant.setStockQuantity(variantReq.getStockQuantity());
        // variant.setImageUrl(variantReq.getImageUrl());
        // variant.setProduct(product);
        // return variant;
        // }).collect(Collectors.toList());

        // product.setVariants(variants);
        return product;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getRandomProducts(int limit) {
        try {
            if (limit <= 0) {
                return Collections.emptyList();
            }

            List<Product> activeProducts = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getIsActive() == null || p.getIsActive())
                    .collect(Collectors.toList());

            if (activeProducts.isEmpty()) {
                return Collections.emptyList();
            }

            Collections.shuffle(activeProducts);
            return activeProducts.subList(0, Math.min(limit, activeProducts.size()));
        } catch (Exception e) {
            System.err.println("Error in getRandomProducts: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Product getProductBytitle(String title) {
        return productRepository.findBytitle(title).orElse(null);
    }

    public boolean addProduct(ProductRequest request) {
        // Validate required fields
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return false; // Will be caught by validation in controller
        }

        // Check if product already exists
        if (productRepository.findBytitle(request.getTitle()).isPresent()) {
            return false;
        }

        // Validate price constraint: current price must be between 30%-150% of original
        Double originalPrice = request.getOriginalPrice();
        Double currentPrice = request.getCurrentPrice();
        if (originalPrice == null || currentPrice == null) {
            return false;
        }

        Double minPrice = originalPrice * 0.3;
        Double maxPrice = originalPrice * 1.5;
        if (currentPrice < minPrice || currentPrice > maxPrice) {
            return false; // Price out of valid range
        }

        Product product = toEntity(request);
        product.setIsActive(true); // New products are active by default
        productRepository.save(product);

        // Log product creation in history
        logProductHistory(product, "ADD_PRODUCT",
                null, "Product created: " + product.getTitle(), null, "New product added to system");

        return true;
    }

    /**
     * Delete a single product
     * 
     * @return "DELETED" if permanently deleted, "DEACTIVATED" if stock > 0, null if
     *         not found
     */
    public String deleteProduct(Integer productId) {
        if (productRepository.findById(productId).isEmpty()) {
            return null;
        }

        Product product = productRepository.findById(productId).get();

        // SRS requirement: if stock > 0, deactivate instead of delete
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            product.setIsActive(false);
            productRepository.save(product);

            // Log deactivation in history
            logProductHistory(product, "DEACTIVATE_PRODUCT",
                    null, "isActive: true -> false", null, "Stock > 0, product deactivated instead of deleted");

            return "DEACTIVATED";
        }

        // Only delete if stock = 0
        productRepository.deleteById(productId);

        // Log deletion in history
        logProductHistory(product, "DELETE_PRODUCT",
                null, null, null, "Stock == 0, product permanently deleted");

        return "DELETED";
    }

    /**
     * Activate multiple products (set isActive = true)
     * 
     * @return number of products activated
     */
    public int activateProducts(List<Integer> productIds) {
        int activated = 0;
        for (Integer productId : productIds) {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                if (!product.getIsActive()) {
                    product.setIsActive(true);
                    productRepository.save(product);

                    // Log activation
                    logProductHistory(product, "ACTIVATE_PRODUCT",
                            "isActive: false", "isActive: true", null, "Product reactivated by admin");

                    activated++;
                }
            }
        }
        return activated;
    }

    /**
     * Batch delete products with business rules:
     * - Max 10 products per batch
     * - Max 20 products per day
     * - If stock > 0: deactivate instead of delete
     * - If stock == 0: permanently delete
     */
    public BatchDeleteResponse batchDeleteProducts(List<Integer> productIds) {
        List<ProductDeletionDetail> details = new ArrayList<>();
        int deleted = 0;
        int deactivated = 0;
        int failed = 0;
        int limitExceeded = 0;

        // Validate batch size (max 10 per request)
        if (productIds.size() > MAX_BATCH_DELETE) {
            return BatchDeleteResponse.builder()
                    .totalRequested(productIds.size())
                    .deleted(0)
                    .deactivated(0)
                    .failed(0)
                    .limitExceeded(productIds.size())
                    .message("Batch size exceeds limit. Maximum " + MAX_BATCH_DELETE + " products per request.")
                    .details(Collections.emptyList())
                    .build();
        }

        // Check daily deletion limit (max 20 per day)
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long deletedToday = productHistoryRepository.countDeletionsByDateRange(startOfDay, endOfDay);

        long remainingQuota = MAX_DAILY_DELETE - deletedToday;
        if (remainingQuota <= 0) {
            return BatchDeleteResponse.builder()
                    .totalRequested(productIds.size())
                    .deleted(0)
                    .deactivated(0)
                    .failed(0)
                    .limitExceeded(productIds.size())
                    .message("Daily deletion limit reached. Maximum " + MAX_DAILY_DELETE
                            + " products per day. Already deleted: " + deletedToday)
                    .details(Collections.emptyList())
                    .build();
        }

        // Process only products within remaining quota
        int processableCount = (int) Math.min(productIds.size(), remainingQuota);
        List<Integer> processableIds = productIds.subList(0, processableCount);
        List<Integer> exceededIds = productIds.subList(processableCount, productIds.size());

        // Process each product
        for (Integer productId : processableIds) {
            Optional<Product> productOpt = productRepository.findById(productId);

            if (productOpt.isEmpty()) {
                failed++;
                details.add(ProductDeletionDetail.builder()
                        .productId(productId)
                        .productTitle("Unknown")
                        .status("FAILED")
                        .reason("Product not found")
                        .build());
                continue;
            }

            Product product = productOpt.get();
            String productTitle = product.getTitle();

            // Check stock quantity
            if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
                // Deactivate instead of delete
                product.setIsActive(false);
                productRepository.save(product);
                deactivated++;

                // Log deactivation
                logProductHistory(product, "DEACTIVATE_PRODUCT",
                        "isActive: true", "isActive: false", null,
                        "Stock > 0 (" + product.getStockQuantity() + "), deactivated instead of deleted");

                details.add(ProductDeletionDetail.builder()
                        .productId(productId)
                        .productTitle(productTitle)
                        .status("DEACTIVATED")
                        .reason("Stock quantity > 0 (" + product.getStockQuantity() + ")")
                        .build());
            } else {
                // Delete permanently
                productRepository.deleteById(productId);
                deleted++;

                // Log deletion
                logProductHistory(product, "DELETE_PRODUCT",
                        null, null, null, "Stock == 0, permanently deleted");

                details.add(ProductDeletionDetail.builder()
                        .productId(productId)
                        .productTitle(productTitle)
                        .status("DELETED")
                        .reason("Stock quantity == 0")
                        .build());
            }
        }

        // Add exceeded products to details
        for (Integer productId : exceededIds) {
            limitExceeded++;
            details.add(ProductDeletionDetail.builder()
                    .productId(productId)
                    .productTitle("N/A")
                    .status("LIMIT_EXCEEDED")
                    .reason("Daily deletion limit reached (" + MAX_DAILY_DELETE + "/day). Already processed: "
                            + deletedToday)
                    .build());
        }

        String message = String.format(
                "Processed %d of %d products: %d deleted, %d deactivated, %d failed, %d limit exceeded",
                processableCount, productIds.size(), deleted, deactivated, failed, limitExceeded);

        return BatchDeleteResponse.builder()
                .totalRequested(productIds.size())
                .deleted(deleted)
                .deactivated(deactivated)
                .failed(failed)
                .limitExceeded(limitExceeded)
                .message(message)
                .details(details)
                .build();
    }

    /**
     * Log product change to ProductHistory
     */
    private void logProductHistory(Product product, String action, String oldValue,
            String newValue, Integer changedBy, String reason) {
        ProductHistory history = ProductHistory.builder()
                .product(product)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .changedDate(LocalDateTime.now())
                .reason(reason)
                .build();

        productHistoryRepository.save(history);
    }

    public void updateProduct(ProductRequest request) {
        // Load product
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Store old values for history logging
        String oldTitle = product.getTitle();
        Double oldPrice = product.getCurrentPrice();
        String oldDescription = product.getDescription();
        Boolean oldIsActive = product.getIsActive();

        // Validate price constraint: current price must be between 30%-150% of original
        Double originalPrice = request.getOriginalPrice() != null ? request.getOriginalPrice()
                : product.getOriginalPrice();
        Double currentPrice = request.getCurrentPrice();

        if (originalPrice != null && currentPrice != null) {
            Double minPrice = originalPrice * 0.3;
            Double maxPrice = originalPrice * 1.5;
            if (currentPrice < minPrice || currentPrice > maxPrice) {
                throw new IllegalArgumentException("Price must be between 30% and 150% of original value");
            }
        }

        // Update basic info
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setWeight(request.getWeight());
        product.setCurrentPrice(request.getCurrentPrice());
        product.setOriginalPrice(originalPrice);

        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        // Update category and brand
        // if (request.getCategoryName() != null) {
        // Integer categoryId =
        // categoryRepository.findByCategoryNameIgnoreCase(request.getCategoryName())
        // .orElseThrow(() -> new EntityNotFoundException("Category not found"))
        // .getCategoryId();
        // product.setCategoryId(categoryId);
        // }

        // Brand is optional: set if provided and found, otherwise set to null
        // if (request.getBrandName() != null) {
        // Integer brandId =
        // brandRepository.findByBrandNameIgnoreCase(request.getBrandName())
        // .map(b -> b.getBrandId())
        // .orElse(null);
        // product.setBrandId(brandId);
        // }

        // Handle variants
        // if (request.getVariants() != null) {
        // Set<Integer> updatedVariantIds = request.getVariants().stream()
        // .filter(v -> v.getVariantId() != null)
        // .map(ProductVariantRequest::getVariantId)
        // .collect(Collectors.toSet());

        // // Remove variants not in request
        // product.getVariants().removeIf(v ->
        // !updatedVariantIds.contains(v.getVariantId()));

        // // Create map for existing variants
        // Map<Integer, ProductVariant> existingVariants =
        // product.getVariants().stream()
        // .collect(Collectors.toMap(ProductVariant::getVariantId,
        // Function.identity()));
        // for (ProductVariant productVariant : existingVariants.values()) {
        // }
        // // Update or create variants
        // for (ProductVariantRequest variantReq : request.getVariants()) {
        // ProductVariant variant = variantReq.getVariantId() != null
        // ? existingVariants.get(variantReq.getVariantId())
        // : null;
        // if (variant == null) {
        // variant = new ProductVariant();
        // variant.setProduct(product);
        // product.getVariants().add(variant);
        // }
        // variant.setColor(variantReq.getColor());
        // variant.setImageUrl(variantReq.getImageUrl());
        // variant.setStockQuantity(variantReq.getStockQuantity());
        // variant.setDiscountPercentage(variantReq.getDiscountPercentage());
        // }
        // }
        productRepository.save(product);

        // Log product update in history
        StringBuilder changes = new StringBuilder();
        if (!oldTitle.equals(request.getTitle())) {
            changes.append("Title: ").append(oldTitle).append(" -> ").append(request.getTitle()).append("; ");
        }
        if (!oldPrice.equals(request.getCurrentPrice())) {
            changes.append("Price: ").append(oldPrice).append(" -> ").append(request.getCurrentPrice()).append("; ");
        }
        if (!oldDescription.equals(request.getDescription())) {
            changes.append("Description updated; ");
        }
        if (!oldIsActive.equals(request.getIsActive())) {
            changes.append("Status: ").append(oldIsActive).append(" -> ").append(request.getIsActive()).append("; ");
        }

        if (changes.length() > 0) {
            logProductHistory(product, "UPDATE_PRODUCT",
                    changes.toString(), "Product updated", null, "Product information modified");
        }
    }

    public ProductDTO getProductById(Integer productId) {
        return productRepository.findById(productId).map(p -> toDTO(p)).orElse(null);
    }

    // public List<ProductDTO> getProductsByCategory(String categoryName) {
    // Optional<Category> category =
    // categoryRepository.findByCategoryNameIgnoreCase(categoryName);
    // if (category.isPresent()) {
    // List<Product> products =
    // productRepository.findByCategoryId(category.get().getCategoryId());
    // List<ProductDTO> productDTOs = new ArrayList<>();
    // for (Product product : products) {
    // ProductDTO dto = toDTO(product);
    // productDTOs.add(dto);
    // }
    // return productDTOs;
    // } else
    // return null;

    // }

    // public List<ProductDTO> getProductByBrand(String brandName) {
    // Optional<Brand> brand = brandRepository.findByBrandNameIgnoreCase(brandName);
    // if (brand.isPresent()) {
    // List<Product> products =
    // productRepository.findByBrandId(brand.get().getBrandId());
    // List<ProductDTO> productDTOs = new ArrayList<>();
    // for (Product product : products) {
    // ProductDTO dto = toDTO(product);
    // productDTOs.add(dto);
    // }
    // return productDTOs;
    // } else
    // return null;

    // }

    public List<ProductDTO> searchProductsByName(String search) {

        List<Product> products = productRepository.findBytitleContainingIgnoreCase(search);
        List<ProductDTO> productDTOs = new ArrayList<>();
        for (Product product : products) {
            if (product.getTitle() != null && !product.getTitle().isEmpty()) {
                ProductDTO dto = toDTO(product);
                productDTOs.add(dto);
            }
        }
        return productDTOs;
    }

    public List<ProductDTO> searchProductsByName(String search, String category) {
        if (search == null || search.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String trimmedSearch = search.trim();
        List<Product> products = productRepository.findBytitleContainingIgnoreCase(trimmedSearch);
        List<ProductDTO> productDTOs = new ArrayList<>();

        System.out.println("[SearchDebug] Search term: " + trimmedSearch);
        System.out.println("[SearchDebug] Category filter: " + category);
        System.out.println("[SearchDebug] Found " + products.size() + " products matching search term");

        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            // Map category variations (Sách -> Book, Báo -> Newspaper, etc.)
            String normalizedCategory = category.trim();
            if (normalizedCategory.equalsIgnoreCase("Sách")) {
                normalizedCategory = "Book";
            } else if (normalizedCategory.equalsIgnoreCase("Báo")) {
                normalizedCategory = "Newspaper";
            }

            System.out.println("[SearchDebug] Normalized category: " + normalizedCategory);

            for (Product product : products) {
                System.out.println("[SearchDebug] Checking product - ID: " + product.getProductId()
                        + ", Title: " + product.getTitle()
                        + ", Category: " + product.getCategory()
                        + ", IsActive: " + product.getIsActive());

                // Only add active products
                if (product.getTitle() != null && !product.getTitle().isEmpty() &&
                        (product.getIsActive() == null || product.getIsActive())) {

                    if (product.getCategory() != null && product.getCategory().equalsIgnoreCase(normalizedCategory)) {
                        System.out.println("[SearchDebug] MATCH! Adding product: " + product.getTitle());
                        ProductDTO dto = toDTO(product);
                        productDTOs.add(dto);
                    } else {
                        System.out.println("[SearchDebug] NO MATCH - Category mismatch. Expected: " + normalizedCategory
                                + ", Got: " + product.getCategory());
                    }
                } else {
                    System.out.println("[SearchDebug] SKIP - Product is inactive or title is empty. IsActive: "
                            + product.getIsActive());
                }
            }
        } else {
            // No category filter, return all matching products (only active ones)
            System.out.println("[SearchDebug] No category filter - returning all matching active products");
            for (Product product : products) {
                if (product.getTitle() != null && !product.getTitle().isEmpty() &&
                        (product.getIsActive() == null || product.getIsActive())) {
                    ProductDTO dto = toDTO(product);
                    productDTOs.add(dto);
                }
            }
        }

        System.out.println("[SearchDebug] Final result: " + productDTOs.size() + " products");
        return productDTOs;
    }

    // public List<ProductDTO> getProductByCategoryAndBrand(String categoryName,
    // String brandName) {
    // Optional<Category> catOpt =
    // categoryRepository.findByCategoryNameIgnoreCase(categoryName);
    // // Optional<Brand> brandOpt =
    // brandRepository.findByBrandNameIgnoreCase(brandName);

    // List<Product> products = new ArrayList<>();
    // if (catOpt.isPresent() && brandOpt.isPresent()) {
    // Integer categoryId = catOpt.get().getCategoryId();
    // Integer brandId = brandOpt.get().getBrandId();
    // products = productRepository.findProductsByCategoryIdAndBrandId(categoryId,
    // brandId);
    // } else if (catOpt.isPresent()) {
    // products = productRepository.findByCategoryId(catOpt.get().getCategoryId());
    // } else {
    // // nothing found
    // products = Collections.emptyList();
    // }
    // List<ProductDTO> productDTOs = new ArrayList<>();
    // for (Product product : products) {
    // ProductDTO dto = toDTO(product);
    // productDTOs.add(dto);
    // }
    // return productDTOs;
    // }

    public ProductQuantityCheckResponse checkProductQuantity(ProductQuantityCheckRequest request) {
        ProductQuantityCheckResponse response = new ProductQuantityCheckResponse();
        Optional<Product> product = productRepository.findById(request.getProductId());
        if (product.isPresent()) {
            response.setProductId(request.getProductId());
            response.setQuantity(product.get().getStockQuantity());
            return response;
        } else
            return null;
    }

    // public boolean deleteVariant(Integer variantId) {
    // if (!productVariantRepository.existsById(variantId)) {
    // return false;
    // }
    // productVariantRepository.deleteById(variantId);
    // return true;
    // }

    private String normalize(String input) {
        if (input == null)
            return "";

        // Chuẩn hóa cơ bản
        String normalized = input.trim().toLowerCase().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("(?<=\\d)(gb|tb|mb|hz)", " $1");

        // Nếu bắt đầu bằng Apple → giữ nguyên sau chuẩn hóa
        if (normalized.startsWith("apple")) {
            return normalized;
        }

        // Nếu bắt đầu bằng Intel hoặc AMD → lấy 3 từ đầu
        if (normalized.startsWith("intel") || normalized.startsWith("amd")) {
            String[] parts = normalized.split("\\s+");
            int limit = Math.min(parts.length, 3);
            return String.join(" ", Arrays.copyOfRange(parts, 0, limit));
        }

        // Xử lý đặc biệt cho RAM / Storage như cũ
        if (normalized.matches("^\\d+\\s+(gb|tb|mb|hz)\\b.*")) {
            String[] parts = normalized.split("\\s+");
            if (parts.length >= 2 && parts[1].matches("gb|tb|hz")) {
                normalized = parts[0] + " " + parts[1];
            }
        }

        return normalized;
    }

    // Cache để tăng hiệu suất
    private final Map<String, List<Map<String, String>>> specCache = new ConcurrentHashMap<>();

    public List<ProductDTO> searchProductsWithFilter(SearchFilterRequest request) {
        // Start with all active products
        List<Product> allProducts = productRepository.findAll().stream()
                .filter(Product::getIsActive)
                .collect(java.util.stream.Collectors.toList());

        // Apply search term filter
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            String searchTerm = request.getSearchTerm().trim().toLowerCase();
            allProducts = allProducts.stream()
                    .filter(product -> product.getTitle() != null &&
                            product.getTitle().toLowerCase().contains(searchTerm))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply category filter
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String category = request.getCategory().trim().toLowerCase();
            allProducts = allProducts.stream()
                    .filter(product -> product.getCategory() != null &&
                            product.getCategory().toLowerCase().contains(category))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply price range filter
        if (request.getLowerBound() != null || request.getUpperBound() != null) {
            Long lowerBound = request.getLowerBound() != null ? request.getLowerBound() : 0L;
            Long upperBound = request.getUpperBound() != null ? request.getUpperBound() : Long.MAX_VALUE;

            allProducts = allProducts.stream()
                    .filter(product -> {
                        Double price = product.getCurrentPrice();
                        return price != null && price >= lowerBound && price <= upperBound;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply sorting
        if (request.getSort() != null && !request.getSort().trim().isEmpty()) {
            String sort = request.getSort().trim();
            switch (sort) {
                case "price_asc":
                    allProducts.sort((a, b) -> {
                        Double priceA = a.getCurrentPrice() != null ? a.getCurrentPrice() : 0.0;
                        Double priceB = b.getCurrentPrice() != null ? b.getCurrentPrice() : 0.0;
                        return priceA.compareTo(priceB);
                    });
                    break;
                case "price_desc":
                    allProducts.sort((a, b) -> {
                        Double priceA = a.getCurrentPrice() != null ? a.getCurrentPrice() : 0.0;
                        Double priceB = b.getCurrentPrice() != null ? b.getCurrentPrice() : 0.0;
                        return priceB.compareTo(priceA);
                    });
                    break;
                case "name_asc":
                    allProducts.sort((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : "";
                        String titleB = b.getTitle() != null ? b.getTitle() : "";
                        return titleA.compareToIgnoreCase(titleB);
                    });
                    break;
                case "name_desc":
                    allProducts.sort((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : "";
                        String titleB = b.getTitle() != null ? b.getTitle() : "";
                        return titleB.compareToIgnoreCase(titleA);
                    });
                    break;
                default:
                    // Default sort by title ascending
                    allProducts.sort((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : "";
                        String titleB = b.getTitle() != null ? b.getTitle() : "";
                        return titleA.compareToIgnoreCase(titleB);
                    });
                    break;
            }
        }

        // Apply pagination
        int page = request.getPage() != null ? request.getPage() : 1;
        int limit = request.getLimit() != null ? request.getLimit() : 20;

        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, allProducts.size());

        List<Product> paginatedProducts;
        if (startIndex >= allProducts.size()) {
            paginatedProducts = new ArrayList<>();
        } else {
            paginatedProducts = allProducts.subList(startIndex, endIndex);
        }

        // Convert to DTOs
        List<ProductDTO> productDTOs = new ArrayList<>();
        for (Product product : paginatedProducts) {
            productDTOs.add(toDTO(product));
        }

        return productDTOs;
    }

    // deepseek laptop &
    // smartphone----------------------------------------------------------------------------------------

    public List<Product> getProductsWithFilterLaptop(SearchFilterRequest request) {
        // Lấy tất cả sản phẩm thuộc danh mục Laptop (categoryId = 1)
        List<Product> allProducts = productRepository.findAll();
        List<Product> filteredProducts = new ArrayList<>();
        // Duyệt qua từng sản phẩm trong danh sách
        for (Product product : allProducts) {
            // if (request.getBrandId() != null && !Objects.equals(product.getBrandId(),
            // request.getBrandId()))
            // continue;
            if (product.getCurrentPrice() > request.getUpperBound()
                    || product.getCurrentPrice() < request.getLowerBound())
                continue;
            // Lấy thông số kỹ thuật của sản phẩm

            // Kiểm tra xem sản phẩm có khớp với các tiêu chí filter không

            // Nếu khớp thì thêm vào danh sách kết quả
            // if (isMatch) {
            // filteredProducts.add(product);
            // }
        }

        // Trả về danh sách sản phẩm đã lọc
        return filteredProducts;
    }

    public List<Product> getProductsWithFilterSmartPhone(SearchFilterRequest request) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> filteredProducts = new ArrayList<>();

        // Duyệt qua từng sản phẩm trong danh sách
        for (Product product : allProducts) {
            // if (request.getBrandId() != null && !Objects.equals(product.getBrandId(),
            // request.getBrandId()))
            // continue;
            if (product.getCurrentPrice() > request.getUpperBound()
                    || product.getCurrentPrice() < request.getLowerBound())
                continue;
            // Lấy thông số kỹ thuật của sản phẩm

            // Kiểm tra xem sản phẩm có khớp với các tiêu chí filter không

            // Nếu khớp thì thêm vào danh sách kết quả
            // if (isMatch) {
            // filteredProducts.add(product);
            // }
        }

        // Trả về danh sách sản phẩm đã lọc
        return filteredProducts;
    }

    // private boolean matchSpecificationsLaptop(String jsonSpec,
    // SearchFilterRequest request) {
    // try {
    // ObjectMapper mapper = new ObjectMapper();
    // List<Map<String, String>> specs = mapper.readValue(jsonSpec,
    // new TypeReference<List<Map<String, String>>>() {
    // });

    // // Kiểm tra CPU (nếu request có danh sách CPU)
    // boolean cpuMatch = request.getCpu() == null || request.getCpu().isEmpty() ||
    // matchesAnyField(specs, Arrays.asList(
    // "Công nghệ CPU", "CPU", "Processor", "Bộ vi xử lý", "Chip", "Chip xử lý", "Vi
    // xử lý"),
    // request.getCpu(), FieldType.CPU);

    // // Kiểm tra RAM (nếu request có danh sách RAM)
    // boolean memoryMatch = request.getMemory() == null ||
    // request.getMemory().isEmpty() ||
    // matchesAnyField(specs, Arrays.asList(
    // "RAM", "Bộ nhớ RAM", "Dung lượng RAM", "Memory", "System Memory"),
    // request.getMemory(), FieldType.MEMORY);

    // // Kiểm tra Storage (nếu request có danh sách Storage)
    // boolean storageMatch = request.getStorage() == null ||
    // request.getStorage().isEmpty() ||
    // matchesAnyField(specs, Arrays.asList(
    // "Ổ cứng", "Storage", "Bộ nhớ trong", "SSD", "HDD", "Dung lượng lưu trữ",
    // "Hard Drive",
    // "Loại ổ cứng"),
    // request.getStorage(), FieldType.STORAGE);

    // // Kiểm tra Refresh Rate (giữ nguyên dạng String)
    // boolean refreshRateMatch = request.getRefreshRate() == null ||
    // request.getRefreshRate().isEmpty() ||
    // matchesAnyField(specs,
    // Arrays.asList("Tần số quét", "Tốc độ làm tươi", "Refresh rate", "Tốc độ làm
    // mới"),
    // request.getRefreshRate(), FieldType.REFRESH_RATE);

    // return cpuMatch && memoryMatch && storageMatch && refreshRateMatch;
    // } catch (Exception e) {
    // return false;
    // }
    // }

    // private boolean matchSpecificationsSmartPhone(String jsonSpec,
    // SearchFilterRequest request) {
    // try {
    // ObjectMapper mapper = new ObjectMapper();
    // List<Map<String, String>> specs = mapper.readValue(jsonSpec,
    // new TypeReference<List<Map<String, String>>>() {
    // });

    // // Kiểm tra RAM (nếu request có danh sách RAM)
    // boolean memoryMatch = request.getMemory() == null ||
    // request.getMemory().isEmpty() ||
    // matchesAnyField(specs, Arrays.asList("RAM", "Bộ nhớ RAM", "Dung lượng RAM"),
    // request.getMemory(), FieldType.MEMORY);

    // // Kiểm tra Storage (nếu request có danh sách Storage)
    // boolean storageMatch = request.getStorage() == null ||
    // request.getStorage().isEmpty() ||
    // matchesAnyField(specs,
    // Arrays.asList("Ổ cứng", "Dung lượng ổ cứng", "Bộ nhớ trong", "Lưu trữ",
    // "Dung lượng lưu trữ"),
    // request.getStorage(), FieldType.STORAGE);

    // // Kiểm tra Refresh Rate (nếu request có danh sách Refresh Rate)
    // boolean refreshRateMatch = request.getRefreshRate() == null ||
    // request.getRefreshRate().isEmpty() ||
    // matchesAnyField(specs, Arrays.asList("Tần số quét", "Tốc độ làm tươi",
    // "Refresh rate"),
    // request.getRefreshRate(), FieldType.REFRESH_RATE);

    // return memoryMatch && storageMatch && refreshRateMatch;
    // } catch (Exception e) {
    // return false;
    // }
    // }

    private boolean matchesAnyField(List<Map<String, String>> specs, List<String> titleList,
            List<String> expectedValues, FieldType fieldType) {
        if (expectedValues == null || expectedValues.isEmpty()) {
            return true;
        }

        // Chỉ cần khớp với 1 trong các giá trị expected là đủ
        for (String expectedValue : expectedValues) {
            if (matchesField(specs, titleList, expectedValue, fieldType)) {
                return true;
            }
        }
        return false;
    }

    enum FieldType {
        CPU,
        MEMORY,
        STORAGE,
        REFRESH_RATE,
    }

    private boolean matchesField(List<Map<String, String>> specs, List<String> titleList,
            String expectedValue, FieldType fieldType) {
        if (expectedValue == null || expectedValue.trim().isEmpty()) {
            return true;
        }

        String normalizedExpected = normalize(expectedValue);

        for (Map<String, String> spec : specs) {
            String specTitle = spec.get("title");
            String specContent = spec.get("content");
            if (specTitle != null && specContent != null) {
                for (String title : titleList) {
                    if (similarTo(title, specTitle)) {
                        String normalizedContent = normalize(specContent);
                        switch (fieldType) {
                            case CPU:
                                return matchCpu(normalizedContent, normalizedExpected);
                            case MEMORY:
                                return matchMemory(normalizedContent, normalizedExpected);
                            case STORAGE:
                                return matchStorage(normalizedContent, normalizedExpected);
                            case REFRESH_RATE:
                                return matchRefreshRate(normalizedContent, normalizedExpected);
                            default:
                                return normalizedContent.equals(normalizedExpected);
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean matchCpu(String content, String expected) {
        String lowerContent = content.toLowerCase();
        String lowerExpected = expected.toLowerCase();
        return lowerContent.equals(lowerExpected);
    }

    private boolean matchMemory(String content, String expected) {
        String lowerContent = content.toLowerCase();
        String lowerExpected = expected.toLowerCase();
        return lowerContent.equals(lowerExpected);
    }

    private boolean matchStorage(String content, String expected) {
        String lowerContent = content.toLowerCase();
        String lowerExpected = expected.toLowerCase();

        return lowerContent.equals(lowerExpected);
    }

    private boolean matchRefreshRate(String content, String expected) {
        String lowerContent = content.toLowerCase();
        String lowerExpected = expected.toLowerCase();
        return lowerContent.equals(lowerExpected);
    }

    private boolean similarTo(String title1, String title2) {
        String norm1 = title1.toLowerCase();
        String norm2 = title2.toLowerCase();
        return norm1.equals(norm2);
    }

    /**
     * Get all product history records with product information
     * This is an optimized endpoint to avoid multiple API calls
     */
    public List<Map<String, Object>> getAllProductHistory() {
        List<ProductHistory> histories = productHistoryRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductHistory history : histories) {
            Map<String, Object> historyMap = new HashMap<>();
            historyMap.put("id", history.getId());
            historyMap.put("action", history.getAction());
            historyMap.put("oldValue", history.getOldValue());
            historyMap.put("newValue", history.getNewValue());
            historyMap.put("changedBy", history.getChangedBy());
            historyMap.put("changedDate", history.getChangedDate());
            historyMap.put("reason", history.getReason());

            if (history.getProduct() != null) {
                historyMap.put("product", Map.of(
                        "productId", history.getProduct().getProductId(),
                        "title", history.getProduct().getTitle(),
                        "category", history.getProduct().getCategory()));
            }

            result.add(historyMap);
        }

        // Sort by changed date descending
        result.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("changedDate");
            LocalDateTime dateB = (LocalDateTime) b.get("changedDate");
            if (dateA != null && dateB != null) {
                return dateB.compareTo(dateA);
            }
            return 0;
        });

        return result;
    }

    public void validatePriceConstraint(Double originalPrice, Double currentPrice) {
        if (originalPrice == null || currentPrice == null) {
            throw new IllegalArgumentException("Giá gốc và giá bán không được để trống");
        }
        if (originalPrice <= 0) {
            throw new IllegalArgumentException("Giá gốc phải lớn hơn 0");
        }
        Double minPrice = originalPrice * 0.3;
        Double maxPrice = originalPrice * 1.5;
        if (currentPrice < minPrice || currentPrice > maxPrice) {
            throw new IllegalArgumentException(
                    String.format("Giá bán phải từ %.0fđ (30%%) đến %.0fđ (150%%) của giá gốc", minPrice, maxPrice));
        }
    }
}
