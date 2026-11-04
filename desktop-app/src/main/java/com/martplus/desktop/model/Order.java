package com.martplus.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing an Order in the system.
 * Demonstrates aggregation of OrderItems, encapsulation of total amount and order states.
 */
public class Order {
    private Long id;
    private Long buyerId;
    private String buyerName;  // Joined projection
    private String buyerEmail; // Joined projection
    private Long shippingAddressId;
    private String shippingAddressText; // Cached/joined address text
    private BigDecimal totalAmount;
    private String status;        // PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String paymentStatus; // PENDING, COMPLETED, FAILED
    private String paymentMethod; // COD, CREDIT_CARD, UPI, NET_BANKING
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Associated items (demonstrating Collections: List<OrderItem>)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Long buyerId, Long shippingAddressId, BigDecimal totalAmount,
                 String status, String paymentStatus, String paymentMethod,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.shippingAddressId = shippingAddressId;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.status = status != null ? status : "PLACED";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PENDING";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "COD";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
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

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public String getShippingAddressText() {
        return shippingAddressText;
    }

    public void setShippingAddressText(String shippingAddressText) {
        this.shippingAddressText = shippingAddressText;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}
