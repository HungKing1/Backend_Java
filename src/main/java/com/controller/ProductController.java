/*
* ĐÁNH GIÁ THIẾT KẾ MÔ-ĐUN
* ---------------------------------------------------------
* 1. COUPLING:
* - Mức độ: Data Coupling
* - Với lớp nào: ProductService, ProductRequest, SearchFilterRequest,
* ProductDTO, StatusResponse, JwtService
* - Lý do: Controller nhận request objects (ProductRequest, SearchFilterRequest)
* và gọi ProductService để xử lý business logic. Chỉ truyền data objects
* cần thiết, không chia sẻ internal structure. ProductService trả về
* ProductDTO hoặc results, controller chỉ wrap vào ResponseEntity.
*
* - Mức độ: Stamp Coupling
* - Với lớp nào: SearchFilterRequest
* - Lý do: Method searchProductsInFilter() nhận toàn bộ SearchFilterRequest
* object nhưng chỉ validate và sử dụng một số fields (type, lowerBound,
* upperBound). Không phải tất cả attributes đều được kiểm tra.
*
* - Mức độ: Common Coupling
* - Với lớp nào: Spring Framework
* - Lý do: Sử dụng Spring annotations để define REST endpoints và dependency
* injection. Framework coupling được chấp nhận trong Spring MVC architecture.
*
* 2. COHESION:
* - Mức độ: Functional Cohesion
* - Giữa các thành phần: addProduct(), deleteProduct(), updateProduct(),
* getProduct(), searchProduct(), getAllProducts(), searchProductsInFilter()
* - Lý do: Tất cả methods đều phục vụ một mục đích duy nhất là expose Product
* APIs cho client. Mỗi method handle một specific HTTP request liên quan
* đến Product operations (CRUD và search). Controller chỉ làm nhiệm vụ
* routing requests và format responses, delegate business logic cho service layer.
* ---------------------------------------------------------
*/

package com.controller;

import com.entity.Product;
import com.entity.dto.ProductDTO;
import com.request.ProductRequest;
import com.request.SearchFilterRequest;
import com.response.BatchDeleteResponse;
import com.response.StatusResponse;
import com.service.JwtService;
import com.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request) {
        // Validate required fields
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.status(400).body(new StatusResponse("Tên sản phẩm không được để trống"));
        }
        if (request.getOriginalPrice() == null || request.getOriginalPrice() <= 0) {
            return ResponseEntity.status(400).body(new StatusResponse("Giá gốc phải lớn hơn 0"));
        }
        if (request.getCurrentPrice() == null || request.getCurrentPrice() <= 0) {
            return ResponseEntity.status(400).body(new StatusResponse("Giá bán phải lớn hơn 0"));
        }

        // Validate price constraint: current price must be between 30%-150% of original
        Double minPrice = request.getOriginalPrice() * 0.3;
        Double maxPrice = request.getOriginalPrice() * 1.5;
        if (request.getCurrentPrice() < minPrice || request.getCurrentPrice() > maxPrice) {
            return ResponseEntity.status(400).body(new StatusResponse(
                    String.format("Giá bán phải nằm trong khoảng 30%% - 150%% so với giá gốc (%.0f - %.0f)",
                            minPrice, maxPrice)));
        }

        if (productService.addProduct(request)) {
            return ResponseEntity.status(200).body(productService.getProductBytitle(request.getTitle()));
        } else {
            return ResponseEntity.status(409).body(new StatusResponse("Sản phẩm này đã tồn tại"));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) List<Integer> productIds) {

        // Single product deletion
        if (productId != null) {
            String result = productService.deleteProduct(productId);
            if (result == null) {
                return ResponseEntity.status(404).body(new StatusResponse("Sản phẩm không tồn tại"));
            }
            if ("DEACTIVATED".equals(result)) {
                return ResponseEntity.status(200)
                        .body(new StatusResponse("Sản phẩm đã được vô hiệu hóa (tồn kho > 0)"));
            }
            return ResponseEntity.status(200).body(new StatusResponse("Sản phẩm đã được xóa thành công"));
        }

        // Batch product deletion (max 10 products at once)
        if (productIds != null && !productIds.isEmpty()) {
            BatchDeleteResponse response = productService.batchDeleteProducts(productIds);

            // Return appropriate status based on results
            if (response.getLimitExceeded() > 0 && response.getDeleted() == 0 && response.getDeactivated() == 0) {
                return ResponseEntity.status(400).body(response); // All rejected due to limits
            }

            return ResponseEntity.status(200).body(response);
        }

        // Neither parameter provided
        return ResponseEntity.status(400).body(new StatusResponse("Phải cung cấp productId hoặc productIds"));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestBody ProductRequest request) {
        // Validate required fields
        if (request.getProductId() == null) {
            return ResponseEntity.status(400).body(new StatusResponse("ID sản phẩm không được để trống"));
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.status(400).body(new StatusResponse("Tên sản phẩm không được để trống"));
        }
        if (request.getCurrentPrice() == null || request.getCurrentPrice() <= 0) {
            return ResponseEntity.status(400).body(new StatusResponse("Giá bán phải lớn hơn 0"));
        }

        try {
            productService.updateProduct(request);
            return ResponseEntity.status(200).body(productService.getProductBytitle(request.getTitle()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new StatusResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new StatusResponse("Cập nhật sản phẩm thất bại: " + e.getMessage()));
        }
    }

    @PutMapping("/activate")
    public ResponseEntity<?> activateProducts(@RequestParam List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.status(400).body(new StatusResponse("Phải cung cấp danh sách productIds"));
        }

        if (productIds.size() > 10) {
            return ResponseEntity.status(400)
                    .body(new StatusResponse("Chỉ có thể kích hoạt tối đa 10 sản phẩm cùng lúc"));
        }

        int activated = productService.activateProducts(productIds);
        return ResponseEntity.status(200).body(new StatusResponse(
                String.format("Đã kích hoạt %d sản phẩm thành công", activated)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Integer productId) {
        ProductDTO productDTO = productService.getProductById(productId);
        if (productDTO != null) {
            return ResponseEntity.status(200).body(productDTO);
        } else
            return ResponseEntity.status(404).body(new StatusResponse("This product does not exist"));
    }

    // @GetMapping("/category={category}")
    // public ResponseEntity<?> getProductsByCategory(@PathVariable String category)
    // {
    // List<ProductDTO> products = productService.getProductsByCategory(category);
    // if (products == null)
    // return ResponseEntity.status(404).body(new StatusResponse("This category does
    // not exist"));

    // if (products.isEmpty())
    // return ResponseEntity.status(200).body(new StatusResponse("This category does
    // not have any products"));

    // return ResponseEntity.status(200).body(products);
    // }

    // @GetMapping("/brand={brand}")
    // public ResponseEntity<?> getProductByBrand(@PathVariable String brand) {
    // // List<ProductDTO> products = productService.getProductByBrand(brand);
    // if (products == null)
    // return ResponseEntity.status(404).body(new StatusResponse("This brand does
    // not exist"));

    // if (products.isEmpty())
    // return ResponseEntity.status(200).body(new StatusResponse("This brand does
    // not have any products"));

    // return ResponseEntity.status(200).body(products);
    // }

    // @GetMapping("/{category}/{brand}")
    // public ResponseEntity<?> getProductsByCategoryAndBrand(@PathVariable String
    // category, @PathVariable String brand) {
    // List<ProductDTO> products =
    // productService.getProductByCategoryAndBrand(category, brand);
    // if (products == null)
    // return ResponseEntity.status(404).body(new StatusResponse("Not exist"));
    // if (products.isEmpty()) {
    // return ResponseEntity.status(404).body(new StatusResponse("No products
    // found"));
    // }
    // return ResponseEntity.status(200).body(products);
    // }

    @GetMapping("/search")
    public ResponseEntity<?> searchProduct(@RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page) {
        // Validate search parameter
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.status(400).body(new StatusResponse("Search parameter 'q' is required"));
        }

        List<ProductDTO> products = productService.searchProductsByName(q.trim(), category);
        // Always return array, empty or not
        return ResponseEntity.status(200).body(products);
    }

    // Legacy endpoint for backward compatibility with frontend
    @GetMapping("/search={search}")
    public ResponseEntity<?> searchProductLegacy(@PathVariable String search) {
        if (search == null || search.trim().isEmpty()) {
            return ResponseEntity.status(400).body(new StatusResponse("Search parameter is required"));
        }

        List<ProductDTO> products = productService.searchProductsByName(search.trim(), null);
        if (products.isEmpty()) {
            return ResponseEntity.status(200).body(new StatusResponse("No matching products found"));
        } else
            return ResponseEntity.status(200).body(products);
    }

    // Autocomplete/suggestion endpoint for real-time search suggestions
    @GetMapping("/suggest")
    public ResponseEntity<?> suggestProducts(@RequestParam(required = false) String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.status(200).body(new ArrayList<>()); // Return empty list for empty query
        }

        List<ProductDTO> products = productService.searchProductsByName(q.trim(), null);
        return ResponseEntity.status(200).body(products);
    }

    // @DeleteMapping("/variant/delete")
    // public ResponseEntity<?> deleteVariant(@RequestParam Integer variantId) {
    // if (productService.deleteVariant(variantId)) {
    // return ResponseEntity.status(200).body(new StatusResponse("Variant deleted
    // successfully"));
    // } else
    // return ResponseEntity.status(404).body(new StatusResponse("Variant does not
    // exist"));
    // }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.status(200).body(productService.getAllProducts());
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomProducts(
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                return ResponseEntity.status(200).body(new ArrayList<>());
            }

            return ResponseEntity.status(200).body(productService.getRandomProducts(limit));
        } catch (Exception e) {
            System.err.println("Error in getRandomProducts endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new StatusResponse("Error fetching random products: " + e.getMessage()));
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<?> searchProductsInFilter(@RequestBody SearchFilterRequest request) {
        try {
            // Validate search term if provided
            if (request.getSearchTerm() != null && request.getSearchTerm().trim().isEmpty()) {
                request.setSearchTerm(null);
            }

            // Validate category if provided
            if (request.getCategory() != null && request.getCategory().trim().isEmpty()) {
                request.setCategory(null);
            }

            // Validate price bounds
            if (request.getLowerBound() == null) {
                request.setLowerBound(0L);
            }
            if (request.getUpperBound() == null) {
                request.setUpperBound(Long.MAX_VALUE);
            }
            if (request.getLowerBound() > request.getUpperBound()) {
                return ResponseEntity.status(400)
                        .body(new StatusResponse("Lower bound cannot be greater than upper bound"));
            }

            // Validate pagination
            if (request.getPage() == null || request.getPage() < 1) {
                request.setPage(1);
            }
            if (request.getLimit() == null || request.getLimit() < 1) {
                request.setLimit(20); // Default limit
            }
            if (request.getLimit() > 100) {
                request.setLimit(100); // Maximum limit
            }

            return ResponseEntity.status(200).body(productService.searchProductsWithFilter(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new StatusResponse("Error filtering products: " + e.getMessage()));
        }
    }

    /**
     * Get all product history - optimized endpoint to avoid multiple API calls
     * Returns all product history records with product details
     */
    @GetMapping("/history/all")
    public ResponseEntity<?> getAllProductHistory() {
        try {
            return ResponseEntity.status(200).body(productService.getAllProductHistory());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new StatusResponse("Error fetching product history: " + e.getMessage()));
        }
    }

}
