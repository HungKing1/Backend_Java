package com.repository;

import com.entity.DVD;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DVDRepository extends JpaRepository<DVD, Integer> {
    Optional<DVD> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}
