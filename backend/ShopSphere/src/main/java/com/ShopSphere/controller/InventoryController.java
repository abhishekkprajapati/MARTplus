package com.ShopSphere.controller;

import com.ShopSphere.entity.Product;
import com.ShopSphere.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/inventory")
public class InventoryController {

    private final ProductService productService;

    public InventoryController(ProductService productService) {
        this.productService = productService;
    }

    // Update stock
    @PutMapping("/{productId}/stock")
    public ResponseEntity<?> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer stock,
            Authentication authentication
    ) {

        try {

            Product product = productService.updateStock(
                    productId,
                    stock,
                    authentication.getName()
            );

            return ResponseEntity.ok(product);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts(
            @RequestParam(defaultValue = "5") Integer threshold,
            Authentication authentication
    ) {

        try {

            List<Product> products =
                    productService.getLowStockProducts(
                            authentication.getName(),
                            threshold
                    );

            return ResponseEntity.ok(products);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}