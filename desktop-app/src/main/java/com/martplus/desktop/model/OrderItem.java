package com.martplus.desktop.model;

import java.math.BigDecimal;

/**
 * Model class representing an individual item within an Order.
 */
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    // Joined details for rich display in tables/details
    private String productName;
    private String productCategory;
    private Long sellerId;

    public OrderItem() {
    }

    public OrderItem(Long id, Long orderId, Long productId, Integer quantity,
                     BigDecimal price, BigDecimal subtotal) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity != null ? quantity : 0;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal :
                this.price.multiply(BigDecimal.valueOf(this.quantity));
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null) {
            recalculateSubtotal();
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    private void recalculateSubtotal() {
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
