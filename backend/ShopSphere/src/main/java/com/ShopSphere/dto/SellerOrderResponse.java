package com.ShopSphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerOrderResponse {

    private Long orderId;
    private Long orderItemId;

    private Long productId;
    private String productName;

    private Long buyerId;
    private String buyerName;
    private String buyerEmail;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;

    private LocalDateTime orderCreatedAt;

    public SellerOrderResponse() {
    }

    public SellerOrderResponse(
            Long orderId,
            Long orderItemId,
            Long productId,
            String productName,
            Long buyerId,
            String buyerName,
            String buyerEmail,
            Integer quantity,
            BigDecimal price,
            BigDecimal subtotal,
            String orderStatus,
            String paymentStatus,
            String paymentMethod,
            LocalDateTime orderCreatedAt
    ) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.orderCreatedAt = orderCreatedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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

    public LocalDateTime getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(LocalDateTime orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }
}
