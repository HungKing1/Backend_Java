package com.repository;

import com.entity.DVD;
import com.entity.Newspaper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewspaperRepository extends JpaRepository<Newspaper, Integer> {
    Optional<Newspaper> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}
