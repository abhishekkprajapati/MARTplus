package com.ShopSphere.service;

import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.ProductRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Product createProduct(Product product, String sellerEmail) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException(
                    "Only sellers can create products"
            );
        }

        if (product.getName() == null || product.getName().isBlank()) {
            throw new RuntimeException("Product name is required");
        }

        if (product.getPrice() == null) {
            throw new RuntimeException("Price is required");
        }

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Price cannot be negative");
        }

        if (product.getStock() != null && product.getStock() < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }

        product.setId(null);
        product.setSeller(seller);

        if (product.getStatus() == null ||
                product.getStatus().isBlank()) {
            product.setStatus("ACTIVE");
        } else {
            String status = product.getStatus().toUpperCase();
            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new RuntimeException("Status must be ACTIVE or INACTIVE");
            }
            product.setStatus(status);
        }

        if (product.getStock() == null) {
            product.setStock(0);
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByStatus("ACTIVE");
    }

    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found")
                );
    }

    public List<Product> getProductsBySeller(
            String sellerEmail
    ) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        return productRepository.findBySeller(seller);
    }

    public List<Product> getProductsByCategory(
            String category
    ) {

        return productRepository.findByCategory(category);
    }

    public Product updateProduct(
            Long productId,
            Product updatedProduct,
            String sellerEmail
    ) {

        Product existingProduct = getProductById(productId);

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        if (!existingProduct.getSeller()
                .getId()
                .equals(seller.getId())) {

            throw new RuntimeException(
                    "You can only update your own products"
            );
        }

        if (updatedProduct.getName() != null) {
            if (updatedProduct.getName().isBlank()) {
                throw new RuntimeException("Product name cannot be empty");
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
                throw new RuntimeException("Price cannot be negative");
            }
            existingProduct.setPrice(updatedProduct.getPrice());
        }

        if (updatedProduct.getStock() != null) {
            if (updatedProduct.getStock() < 0) {
                throw new RuntimeException("Stock cannot be negative");
            }
            existingProduct.setStock(updatedProduct.getStock());
        }

        if (updatedProduct.getCategory() != null) {
            existingProduct.setCategory(updatedProduct.getCategory());
        }

        if (updatedProduct.getImageUrl() != null) {
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
        }

        if (updatedProduct.getStatus() != null &&
                !updatedProduct.getStatus().isBlank()) {

            String status = updatedProduct.getStatus().toUpperCase();
            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new RuntimeException("Status must be ACTIVE or INACTIVE");
            }
            existingProduct.setStatus(status);
        }

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(
            Long productId,
            String sellerEmail
    ) {

        Product product = getProductById(productId);

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        if (!product.getSeller()
                .getId()
                .equals(seller.getId())) {

            throw new RuntimeException(
                    "You can only delete your own products"
            );
        }

        try {
            productRepository.delete(product);
        } catch (Exception e) {
            // If hard delete fails due to existing order history, deactivate it safely
            product.setStatus("INACTIVE");
            productRepository.save(product);
        }
    }

    // ==============================
    // INVENTORY MANAGEMENT
    // ==============================

    public Product updateStock(
            Long productId,
            Integer stock,
            String sellerEmail
    ) {

        if (stock == null || stock < 0) {
            throw new RuntimeException(
                    "Stock cannot be negative"
            );
        }

        Product product = getProductById(productId);

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        if (!product.getSeller()
                .getId()
                .equals(seller.getId())) {

            throw new RuntimeException(
                    "You can only update stock of your own products"
            );
        }

        product.setStock(stock);

        return productRepository.save(product);
    }

    public List<Product> getLowStockProducts(
            String sellerEmail,
            Integer threshold
    ) {

        if (threshold == null || threshold < 0) {
            throw new RuntimeException(
                    "Threshold cannot be negative"
            );
        }

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        return productRepository
                .findBySeller(seller)
                .stream()
                .filter(product ->
                        product.getStock() != null &&
                        product.getStock() <= threshold
                )
                .toList();
    }
}