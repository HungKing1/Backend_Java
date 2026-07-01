package com.repository;

import com.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Optional<Book> findByProductId(Integer productId);
}
