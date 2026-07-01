package com.repository;

import com.entity.CD;
import com.entity.DVD;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CDRepository extends JpaRepository<CD, Integer> {
    Optional<CD> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);

}
