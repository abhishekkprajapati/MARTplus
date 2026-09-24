package com.ShopSphere.service;

import com.ShopSphere.dto.OrderTrackingResponse;
import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.OrderTracking;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.OrderRepository;
import com.ShopSphere.repository.OrderTrackingRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderTrackingService {

    private final OrderTrackingRepository orderTrackingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderTrackingService(
            OrderTrackingRepository orderTrackingRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.orderTrackingRepository = orderTrackingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    private User getBuyer(String email) {

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Buyer not found")
                );

        if (!"BUYER".equalsIgnoreCase(buyer.getRole())) {
            throw new RuntimeException(
                    "Only buyers can view order tracking"
            );
        }

        return buyer;
    }

    public List<OrderTrackingResponse> getTracking(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        return orderTrackingRepository
                .findByOrderOrderByCreatedAtAsc(order)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderTrackingResponse addTrackingStatus(
            Long orderId,
            String status,
            String description
    ) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        OrderTracking tracking = new OrderTracking();

        tracking.setOrder(order);
        tracking.setStatus(status.toUpperCase());
        tracking.setDescription(description);

        OrderTracking savedTracking =
                orderTrackingRepository.save(tracking);

        order.setStatus(status.toUpperCase());
        orderRepository.save(order);

        return toResponse(savedTracking);
    }

    private OrderTrackingResponse toResponse(
            OrderTracking tracking
    ) {

        return new OrderTrackingResponse(
                tracking.getId(),
                tracking.getOrder().getId(),
                tracking.getStatus(),
                tracking.getDescription(),
                tracking.getCreatedAt()
        );
    }
}