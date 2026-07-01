package com.repository;

import com.entity.Order;
import com.entity.OrderItem;
import com.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findOrderByOrderId(Integer id);

    List<Order> findOrdersByUserId(Integer userId);

    List<Order> findOrdersByStatus(OrderStatus status);

    // List<Order> findOrdersByShipperIdIsNull();

    // List<Order> findOrdersByStatusAndShipperIdIsNull(OrderStatus status);

    // List<Order> findOrdersByShipperId(Integer shipperId);

    List<OrderItem> findByOrderId(Integer orderId);

    // Optional<OrderItem> findByOrderIdAndProductIdAndVariantId(
    // Integer orderId, Integer productId, Integer variantId);

    // Optional<OrderItem> findByOrderItemId(Integer orderItemId);
}
