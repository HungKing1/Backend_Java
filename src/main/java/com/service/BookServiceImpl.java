package com.service;

import com.entity.Book;
import com.entity.ProductHistory;
import com.repository.BookRepository;
import com.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book findById(Integer id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.orElse(null);
    }

    @Override
    public Book findByProductId(Integer productId) {
        Optional<Book> book = bookRepository.findByProductId(productId);
        return book.orElse(null);
    }

    @Override
    public Book save(Book book) {
        // Auto-set category to "Book" if not already set
        if (book.getCategory() == null || book.getCategory().isEmpty()) {
            book.setCategory("Book");
        }
        book.setUpdatedAt(java.time.LocalDateTime.now());
        return bookRepository.save(book);
    }

    @Override
    public Book saveWithHistory(Book book, String adjustmentReason, Integer changedBy) {

        Book oldBook = findByProductId(book.getProductId());

        Integer oldStock = oldBook != null ? oldBook.getStockQuantity() : null;
        Integer newStock = book.getStockQuantity();

        if (book.getCategory() == null || book.getCategory().isEmpty()) {
            book.setCategory("Book");
        }
        book.setUpdatedAt(LocalDateTime.now());

        Book savedBook = bookRepository.save(book);

        if (oldStock != null && !oldStock.equals(newStock)) {
            ProductHistory history = ProductHistory.builder()
                    .product(savedBook)
                    .action("UPDATE_STOCK")
                    .oldValue(oldStock.toString())
                    .newValue(newStock.toString())
                    .changedBy(changedBy)
                    .changedDate(LocalDateTime.now())
                    .reason(adjustmentReason)
                    .build();
            productHistoryRepository.save(history);
        }

        return savedBook;
    }

    @Override
    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void deleteByProductId(Integer productId) {
        Optional<Book> book = bookRepository.findByProductId(productId);
        if (book.isPresent()) {
            bookRepository.deleteById(book.get().getProductId());
        }
    }
}
