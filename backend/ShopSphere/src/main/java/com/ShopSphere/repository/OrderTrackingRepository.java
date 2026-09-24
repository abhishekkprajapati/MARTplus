package com.ShopSphere.repository;

import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTrackingRepository
        extends JpaRepository<OrderTracking, Long> {

    List<OrderTracking> findByOrderOrderByCreatedAtAsc(Order order);
}