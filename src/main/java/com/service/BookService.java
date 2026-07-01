package com.service;

import com.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> findAll();

    Book findById(Integer id);

    Book findByProductId(Integer productId);

    Book save(Book book);

    void deleteById(Integer id);

    void deleteByProductId(Integer productId);

    Book saveWithHistory(Book book, String reasonForAdjustment, Integer changedBy);
}
