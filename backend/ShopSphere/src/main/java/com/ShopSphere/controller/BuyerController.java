package com.ShopSphere.controller;

import com.ShopSphere.dto.BuyerDashboardResponse;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buyer")
public class BuyerController {

    private final OrderService orderService;

    public BuyerController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(
            Authentication authentication
    ) {

        try {
            String email = authentication.getName();

            BuyerDashboardResponse response =
                    orderService.getBuyerDashboard(email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}