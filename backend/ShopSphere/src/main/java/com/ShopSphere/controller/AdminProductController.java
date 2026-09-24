package com.ShopSphere.controller;

import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.ProductRepository;
import com.ShopSphere.repository.UserRepository;
import com.ShopSphere.service.ActivityLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public AdminProductController(
            ProductRepository productRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productRepository.findAll()
        );
    }

    // Create product
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestBody Product product,
            Authentication authentication
    ) {

        try {

            if (product.getName() == null || product.getName().isBlank()) {
                return ResponseEntity.badRequest().body("Product name is required");
            }

            if (product.getPrice() == null) {
                return ResponseEntity.badRequest().body("Price is required");
            }

            if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body("Price cannot be negative");
            }

            if (product.getStock() != null && product.getStock() < 0) {
                return ResponseEntity.badRequest().body("Stock cannot be negative");
            }

            product.setId(null);

            // Seller assignment
            if (product.getSeller() != null && product.getSeller().getId() != null) {
                User seller = userRepository.findById(product.getSeller().getId())
                        .orElseThrow(() ->
                                new RuntimeException("Assigned seller not found")
                        );

                if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
                    return ResponseEntity.badRequest()
                            .body("Assigned user must have SELLER role");
                }

                product.setSeller(seller);
            }

            if (product.getStatus() == null ||
                    product.getStatus().isBlank()) {

                product.setStatus("ACTIVE");
            } else {
                String status = product.getStatus().toUpperCase();
                if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                    return ResponseEntity.badRequest()
                            .body("Status must be ACTIVE or INACTIVE");
                }
                product.setStatus(status);
            }

            if (product.getStock() == null) {
                product.setStock(0);
            }

            Product savedProduct =
                    productRepository.save(product);

            if (authentication != null) {
                activityLogService.createLog(
                        authentication.getName(),
                        "PRODUCT_CREATED",
                        "Created product: " + savedProduct.getName()
                );
            }

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
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct,
            Authentication authentication
    ) {

        try {

            Product existingProduct =
                    productRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Product not found"
                                    )
                            );

            if (updatedProduct.getName() != null) {
                if (updatedProduct.getName().isBlank()) {
                    return ResponseEntity.badRequest().body("Product name cannot be empty");
                }
                existingProduct.setName(updatedProduct.getName());
            }

            if (updatedProduct.getDescription() != null) {
                existingProduct.setDescription(
                        updatedProduct.getDescription()
                );
            }

            if (updatedProduct.getPrice() != null) {
                if (updatedProduct.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    return ResponseEntity.badRequest().body("Price cannot be negative");
                }
                existingProduct.setPrice(
                        updatedProduct.getPrice()
                );
            }

            if (updatedProduct.getStock() != null) {
                if (updatedProduct.getStock() < 0) {
                    return ResponseEntity.badRequest().body("Stock cannot be negative");
                }
                existingProduct.setStock(
                        updatedProduct.getStock()
                );
            }

            if (updatedProduct.getCategory() != null) {
                existingProduct.setCategory(
                        updatedProduct.getCategory()
                );
            }

            if (updatedProduct.getImageUrl() != null) {
                existingProduct.setImageUrl(
                        updatedProduct.getImageUrl()
                );
            }

            // Seller reassignment
            if (updatedProduct.getSeller() != null && updatedProduct.getSeller().getId() != null) {
                User seller = userRepository.findById(updatedProduct.getSeller().getId())
                        .orElseThrow(() ->
                                new RuntimeException("Assigned seller not found")
                        );

                if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
                    return ResponseEntity.badRequest()
                            .body("Assigned user must have SELLER role");
                }

                existingProduct.setSeller(seller);
            }

            if (updatedProduct.getStatus() != null &&
                    !updatedProduct.getStatus().isBlank()) {

                String status = updatedProduct.getStatus().toUpperCase();
                if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                    return ResponseEntity.badRequest()
                            .body("Status must be ACTIVE or INACTIVE");
                }
                existingProduct.setStatus(status);
            }

            Product savedProduct =
                    productRepository.save(existingProduct);

            if (authentication != null) {
                activityLogService.createLog(
                        authentication.getName(),
                        "PRODUCT_UPDATED",
                        "Updated product: " + savedProduct.getName()
                );
            }

            return ResponseEntity.ok(savedProduct);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }

    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            Product product =
                    productRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Product not found"
                                    )
                            );

            try {
                productRepository.delete(product);
            } catch (Exception e) {
                // If product is associated with order history, deactivate it
                product.setStatus("INACTIVE");
                productRepository.save(product);
            }

            if (authentication != null) {
                activityLogService.createLog(
                        authentication.getName(),
                        "PRODUCT_DELETED",
                        "Deleted product #" + id
                );
            }

            return ResponseEntity.ok(
                    "Product deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
