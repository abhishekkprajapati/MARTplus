package com.ShopSphere.controller;

import com.ShopSphere.dto.OrderResponse;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestParam Long addressId,
            @RequestParam String paymentMethod,
            Authentication authentication
    ) {
        try {

            OrderResponse order =
                    orderService.placeOrder(
                            authentication.getName(),
                            addressId,
                            paymentMethod
                    );

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            Authentication authentication
    ) {
        try {

            List<OrderResponse> orders =
                    orderService.getMyOrders(
                            authentication.getName()
                    );

            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {

            OrderResponse order =
                    orderService.getOrderById(
                            authentication.getName(),
                            orderId
                    );

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/cancel")
public ResponseEntity<?> cancelOrder(
        @PathVariable Long orderId,
        Authentication authentication
) {
    try {

        OrderResponse order =
                orderService.cancelOrder(
                        authentication.getName(),
                        orderId
                );

        return ResponseEntity.ok(order);

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}
}