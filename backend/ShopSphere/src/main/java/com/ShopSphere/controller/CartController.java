package com.ShopSphere.controller;

import com.ShopSphere.dto.CartItemResponse;
import com.ShopSphere.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication) {
        try {
            List<CartItemResponse> cart =
                    cartService.getCart(authentication.getName());

            return ResponseEntity.ok(cart);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        try {
            CartItemResponse cartItem =
                    cartService.addToCart(
                            authentication.getName(),
                            productId,
                            quantity
                    );

            return ResponseEntity.ok(cartItem);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        try {
            CartItemResponse cartItem =
                    cartService.updateQuantity(
                            authentication.getName(),
                            cartItemId,
                            quantity
                    );

            return ResponseEntity.ok(cartItem);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication
    ) {
        try {
            cartService.removeFromCart(
                    authentication.getName(),
                    cartItemId
            );

            return ResponseEntity.ok(
                    "Cart item removed successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(
            Authentication authentication
    ) {
        try {
            cartService.clearCart(
                    authentication.getName()
            );

            return ResponseEntity.ok(
                    "Cart cleared successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}