package com.ShopSphere.repository;

import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.OrderItem;
// import com.ShopSphere.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProductSellerId(Long sellerId);
}