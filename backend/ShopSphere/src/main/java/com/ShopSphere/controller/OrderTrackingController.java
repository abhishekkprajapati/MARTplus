package com.ShopSphere.controller;

import com.ShopSphere.dto.OrderTrackingResponse;
import com.ShopSphere.service.OrderTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/orders")
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;

    public OrderTrackingController(
            OrderTrackingService orderTrackingService
    ) {
        this.orderTrackingService = orderTrackingService;
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<?> getTracking(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {

            List<OrderTrackingResponse> tracking =
                    orderTrackingService.getTracking(
                            authentication.getName(),
                            orderId
                    );

            return ResponseEntity.ok(tracking);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}