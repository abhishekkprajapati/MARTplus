package com.martplus.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class representing a Product in the MARTplus catalog.
 * Demonstrates strict Encapsulation with defensive validation for price and stock.
 */
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;
    private String status;   // ACTIVE, INACTIVE
    private Long sellerId;
    private String sellerName; // Join projection helper
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(Long id, String name, String description, BigDecimal price,
                   Integer stock, String category, String imageUrl, String status,
                   Long sellerId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.stock = stock != null ? Math.max(0, stock) : 0;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status != null ? status : "ACTIVE";
        this.sellerId = sellerId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock != null ? Math.max(0, stock) : 0;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return "ACTIVE".equalsIgnoreCase(status) && stock != null && stock > 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
