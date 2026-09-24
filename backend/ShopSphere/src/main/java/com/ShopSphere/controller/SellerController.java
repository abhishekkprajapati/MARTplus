package com.ShopSphere.controller;

import com.ShopSphere.dto.SellerAnalyticsResponse;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final OrderService orderService;

    public SellerController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(
            Authentication authentication
    ) {

        try {
            String email = authentication.getName();

            SellerAnalyticsResponse response =
                    orderService.getSellerAnalytics(email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}