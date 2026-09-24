package com.ShopSphere.controller;

import com.ShopSphere.dto.WishlistItemResponse;
import com.ShopSphere.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<?> getWishlist(
            Authentication authentication
    ) {
        try {
            List<WishlistItemResponse> wishlist =
                    wishlistService.getWishlist(
                            authentication.getName()
                    );

            return ResponseEntity.ok(wishlist);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(
            @RequestParam Long productId,
            Authentication authentication
    ) {
        try {
            WishlistItemResponse wishlistItem =
                    wishlistService.addToWishlist(
                            authentication.getName(),
                            productId
                    );

            return ResponseEntity.ok(wishlistItem);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        try {
            wishlistService.removeFromWishlist(
                    authentication.getName(),
                    productId
            );

            return ResponseEntity.ok(
                    "Product removed from wishlist successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearWishlist(
            Authentication authentication
    ) {
        try {
            wishlistService.clearWishlist(
                    authentication.getName()
            );

            return ResponseEntity.ok(
                    "Wishlist cleared successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}