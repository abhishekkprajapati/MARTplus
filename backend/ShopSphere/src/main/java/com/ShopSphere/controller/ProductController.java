package com.ShopSphere.controller;

import com.ShopSphere.entity.Product;
import com.ShopSphere.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Get all active products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    // Get single product
    @GetMapping("/{id}")
public ResponseEntity<?> getProductById(
        @PathVariable Long id
) {

    try {

        Product product =
                productService.getProductById(id);

        return ResponseEntity.ok(product);

    } catch (RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }
}

    // Get products by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable String category
    ) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(category)
        );
    }

    // Get logged-in seller's products
    @GetMapping("/seller/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Product>> getMyProducts(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                productService.getProductsBySeller(
                        authentication.getName()
                )
        );
    }

    // Create product
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> createProduct(
            @RequestBody Product product,
            Authentication authentication
    ) {

        try {

            Product savedProduct =
                    productService.createProduct(
                            product,
                            authentication.getName()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedProduct);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Update product
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            Authentication authentication
    ) {

        try {

            Product updatedProduct =
                    productService.updateProduct(
                            id,
                            product,
                            authentication.getName()
                    );

            return ResponseEntity.ok(updatedProduct);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Delete product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            productService.deleteProduct(
                    id,
                    authentication.getName()
            );

            return ResponseEntity.ok(
                    "Product deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
