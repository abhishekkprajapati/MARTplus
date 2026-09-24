package com.ShopSphere.controller;

import com.ShopSphere.dto.OrderTrackingResponse;
import com.ShopSphere.service.OrderTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderTrackingController {

    private final OrderTrackingService orderTrackingService;

    public AdminOrderTrackingController(
            OrderTrackingService orderTrackingService
    ) {
        this.orderTrackingService = orderTrackingService;
    }

    @PutMapping("/{orderId}/tracking")
    public ResponseEntity<?> updateTrackingStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String description
    ) {
        try {

            OrderTrackingResponse tracking =
                    orderTrackingService.addTrackingStatus(
                            orderId,
                            status,
                            description
                    );

            return ResponseEntity.ok(tracking);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}