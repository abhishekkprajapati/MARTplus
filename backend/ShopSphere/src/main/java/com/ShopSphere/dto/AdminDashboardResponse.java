package com.ShopSphere.dto;

import java.math.BigDecimal;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalBuyers;
    private long totalSellers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            long totalUsers,
            long totalBuyers,
            long totalSellers,
            long totalProducts,
            long totalOrders,
            long pendingOrders,
            long deliveredOrders,
            long cancelledOrders,
            BigDecimal totalRevenue
    ) {
        this.totalUsers = totalUsers;
        this.totalBuyers = totalBuyers;
        this.totalSellers = totalSellers;
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(long totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public long getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
