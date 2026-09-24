package com.ShopSphere.repository;

import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    Optional<Order> findByIdAndBuyer(Long id, User buyer);

    List<Order> findByStatus(String status);
}