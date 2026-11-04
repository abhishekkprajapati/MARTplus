package com.martplus.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing an Order Tracking Milestone / Event.
 */
public class OrderTracking {
    private Long id;
    private Long orderId;
    private String status;
    private String description;
    private LocalDateTime createdAt;

    public OrderTracking() {
    }

    public OrderTracking(Long id, Long orderId, String status, String description, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
