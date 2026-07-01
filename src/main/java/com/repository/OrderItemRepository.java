package com.repository;

import com.entity.OrderItem;
import com.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    public List<OrderItem> findByOrderId(Integer orderId);

}
