package com.ShopSphere.controller;

import com.ShopSphere.dto.OrderResponse;
import com.ShopSphere.service.ActivityLogService;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final ActivityLogService activityLogService;

    public AdminOrderController(
            OrderService orderService,
            ActivityLogService activityLogService
    ) {
        this.orderService = orderService;
        this.activityLogService = activityLogService;
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<?> getAllOrders() {

        try {

            List<OrderResponse> orders =
                    orderService.getAllOrders();

            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId
    ) {

        try {

            OrderResponse order =
                    orderService.getAdminOrderById(orderId);

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            Authentication authentication
    ) {

        try {

            OrderResponse order =
                    orderService.updateOrderStatus(
                            orderId,
                            status
                    );

            activityLogService.createLog(
                    authentication.getName(),
                    "ORDER_STATUS_UPDATED",
                    "Updated order #" + orderId +
                            " status to " + status.toUpperCase()
            );

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Cancel order by admin
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        try {

            OrderResponse order =
                    orderService.cancelOrderByAdmin(orderId);

            activityLogService.createLog(
                    authentication.getName(),
                    "ORDER_CANCELLED",
                    "Cancelled order #" + orderId
            );

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(
            @PathVariable String status
    ) {

        try {

            List<OrderResponse> orders =
                    orderService.getOrdersByStatus(status);

            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}