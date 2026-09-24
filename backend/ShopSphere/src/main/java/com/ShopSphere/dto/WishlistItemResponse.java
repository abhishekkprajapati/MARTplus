package com.ShopSphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;
    private LocalDateTime createdAt;

    public WishlistItemResponse() {
    }

    public WishlistItemResponse(
            Long id,
            Long productId,
            String productName,
            String description,
            BigDecimal price,
            Integer stock,
            String category,
            String imageUrl,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}