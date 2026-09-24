package com.ShopSphere.controller;

import com.ShopSphere.dto.SellerOrderResponse;
import com.ShopSphere.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> getSellerOrders(
            Authentication authentication
    ) {

        try {

            List<SellerOrderResponse> orders =
                    orderService.getSellerOrders(
                            authentication.getName()
                    );

            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
