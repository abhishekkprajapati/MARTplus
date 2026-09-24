package com.ShopSphere.controller;

import com.ShopSphere.dto.SellerAnalyticsResponse;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/analytics")
public class SellerAnalyticsController {

    private final OrderService orderService;

    public SellerAnalyticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> getAnalytics(
            Authentication authentication
    ) {

        try {

            SellerAnalyticsResponse analytics =
                    orderService.getSellerAnalytics(
                            authentication.getName()
                    );

            return ResponseEntity.ok(analytics);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
