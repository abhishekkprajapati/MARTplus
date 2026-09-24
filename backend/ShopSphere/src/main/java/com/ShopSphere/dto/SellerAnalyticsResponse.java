package com.ShopSphere.dto;

import java.math.BigDecimal;

public class SellerAnalyticsResponse {

    private Long totalProducts;
    private Long totalOrders;
    private Long totalItemsSold;

    private BigDecimal totalRevenue;

    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    public SellerAnalyticsResponse() {
    }

    public SellerAnalyticsResponse(
            Long totalProducts,
            Long totalOrders,
            Long totalItemsSold,
            BigDecimal totalRevenue,
            Long pendingOrders,
            Long completedOrders,
            Long cancelledOrders
    ) {
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalItemsSold = totalItemsSold;
        this.totalRevenue = totalRevenue;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(Long totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
}
