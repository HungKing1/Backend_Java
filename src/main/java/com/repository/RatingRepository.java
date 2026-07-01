package com.repository;

import com.entity.Rating;
import com.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    List<Rating> findByProductId(Integer productId);

    Optional<Rating> findByProductIdAndUserId(Integer productId, Integer userId);
}
