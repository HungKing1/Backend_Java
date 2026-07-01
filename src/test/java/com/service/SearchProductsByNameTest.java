package com.service;

import com.entity.Product;
import com.entity.dto.ProductDTO;
import com.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class SearchProductsByNameTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // 1. Smartphone: Active
        Product p1 = new Product();
        p1.setProductId(1);
        p1.setTitle("iPhone 15 Pro");
        p1.setCategory("Smartphone");
        p1.setIsActive(true);

        // 2. Smartphone: Inactive
        Product p2 = new Product();
        p2.setProductId(2);
        p2.setTitle("iPhone 6 Old");
        p2.setCategory("Smartphone");
        p2.setIsActive(false);

        // 3. Book: Active
        Product p3 = new Product();
        p3.setProductId(3);
        p3.setTitle("Harry Potter Vol 1");
        p3.setCategory("Book");
        p3.setIsActive(true);

        // 4. Newspaper: Active
        Product p4 = new Product();
        p4.setProductId(4);
        p4.setTitle("Báo Tuổi Trẻ");
        p4.setCategory("Newspaper");
        p4.setIsActive(true);

        // 5. CD: Active
        Product p5 = new Product();
        p5.setProductId(5);
        p5.setTitle("Album Modern Talking");
        p5.setCategory("CD");
        p5.setIsActive(true);

        // SỬA LỖI: Thêm lenient() để tránh UnnecessaryStubbingException
        lenient().when(productRepository.findBytitleContainingIgnoreCase("iPhone"))
                .thenReturn(Arrays.asList(p1, p2));

        lenient().when(productRepository.findBytitleContainingIgnoreCase("Harry"))
                .thenReturn(Collections.singletonList(p3));

        lenient().when(productRepository.findBytitleContainingIgnoreCase("Tuổi Trẻ"))
                .thenReturn(Collections.singletonList(p4));

        lenient().when(productRepository.findBytitleContainingIgnoreCase("Modern"))
                .thenReturn(Collections.singletonList(p5));

        lenient().when(productRepository.findBytitleContainingIgnoreCase("Unknown"))
                .thenReturn(new ArrayList<>());
    }

    @DisplayName("Test searchProductsByName: Validation, Trimming, Category Mapping & Active Check")
    @ParameterizedTest(name = "{index} => search='{0}', category='{1}' -> Expect={2}")
    @CsvSource(value = {
            "NULL, , 0, Input null trả về rỗng",
            "'', , 0, Input rỗng trả về rỗng",
            "   , , 0, Input toàn khoảng trắng trả về rỗng",
            "iPhone, NULL, 1, Filter Active: Lấy p1, bỏ p2",
            "iPhone, '', 1, Filter Active (Category empty): Lấy p1",
            " iPhone , NULL, 1, Trim search text hoạt động đúng",
            "Harry, Sách, 1, Map 'Sách' thành 'Book' khớp p3",
            "Harry, sách, 1, Map 'sách' (lower) thành 'Book' khớp p3",
            "Harry, Book, 1, Nhập trực tiếp 'Book' khớp p3",
            "Tuổi Trẻ, Báo, 1, Map 'Báo' thành 'Newspaper' khớp p4",
            "Tuổi Trẻ, Newspaper, 1, Nhập trực tiếp 'Newspaper' khớp p4",
            "Modern, CD, 1, Category 'CD' khớp p5",
            "Harry, ' Sách ', 1, Trim category text hoạt động đúng",
            "iPhone, Laptop, 0, Tên đúng nhưng Category sai",
            "Harry, Báo, 0, Tên đúng nhưng sai loại",
            "Modern, DVD, 0, Tên đúng nhưng sai loại",
            "Unknown, , 0, DB không tìm thấy sản phẩm nào"
    }, nullValues = {"NULL"})
    void testSearchProductsByName(String search, String category, int expectedSize, String description) {
        List<ProductDTO> result = productService.searchProductsByName(search, category);
        assertEquals(expectedSize, result.size(), "Failed: " + description);

        if (expectedSize > 0) {
            ProductDTO dto = result.get(0);
            if (search != null && search.contains("iPhone")) {
                assertEquals("iPhone 15 Pro", dto.getTitle());
            } else if (search != null && search.contains("Harry")) {
                assertEquals("Book", dto.getCategory());
            }
        }
    }
}