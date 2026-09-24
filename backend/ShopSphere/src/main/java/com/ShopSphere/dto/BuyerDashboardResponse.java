package com.ShopSphere.dto;

import java.math.BigDecimal;

public class BuyerDashboardResponse {

    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long wishlistItems;
    private long cartItems;
    private BigDecimal totalSpent;

    public BuyerDashboardResponse(
            long totalOrders,
            long pendingOrders,
            long deliveredOrders,
            long cancelledOrders,
            long wishlistItems,
            long cartItems,
            BigDecimal totalSpent
    ) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.wishlistItems = wishlistItems;
        this.cartItems = cartItems;
        this.totalSpent = totalSpent;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public long getWishlistItems() {
        return wishlistItems;
    }

    public long getCartItems() {
        return cartItems;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
}
