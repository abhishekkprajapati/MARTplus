package com.martplus.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing a Wishlist item for a Buyer.
 */
public class WishlistItem {
    private Long id;
    private Long buyerId;
    private Long productId;
    private LocalDateTime createdAt;

    // Joined product details for display
    private Product product;

    public WishlistItem() {
    }

    public WishlistItem(Long id, Long buyerId, Long productId, LocalDateTime createdAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.productId = productId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
