package com.repository;

import com.entity.CartItem;
import com.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findByUserId(Integer userId);

    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    // Giữ lại method cũ cho compatibility
    Optional<CartItem> findCartItemByUserIdAndProductId(Integer userId, Integer productId);

    List<CartItem> findCartItemsByUserId(Integer userId);

    Optional<CartItem> findByCartItemId(Integer cartItemId);
}
