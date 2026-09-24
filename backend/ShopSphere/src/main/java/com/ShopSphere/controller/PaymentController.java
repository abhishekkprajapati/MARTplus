package com.ShopSphere.controller;

import com.ShopSphere.dto.PaymentResponse;
import com.ShopSphere.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {

            PaymentResponse payment =
                    paymentService.createPayment(
                            authentication.getName(),
                            orderId
                    );

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/order/{orderId}/success")
    public ResponseEntity<?> markPaymentSuccessful(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {

            PaymentResponse payment =
                    paymentService.markPaymentSuccessful(
                            authentication.getName(),
                            orderId
                    );

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {

            PaymentResponse payment =
                    paymentService.getPayment(
                            authentication.getName(),
                            orderId
                    );

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}