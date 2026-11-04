package com.martplus.desktop.controller;

import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.InventoryService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller mediating Seller store management operations.
 */
public class SellerController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public SellerController(ProductService productService, InventoryService inventoryService, OrderService orderService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    public List<Product> getMyProducts(Long sellerId) {
        return productService.getProductsBySeller(sellerId);
    }

    public Product addProduct(User seller, String name, String desc, BigDecimal price, int stock, String cat, String img) {
        return productService.createProduct(seller, name, desc, price, stock, cat, img);
    }

    public void updateStock(Long productId, int stock) {
        inventoryService.updateStock(productId, stock);
    }

    public List<Order> getSellerOrders(Long sellerId) {
        return orderService.getOrdersForSeller(sellerId);
    }

    public void updateOrderStatus(User seller, Long orderId, String newStatus) {
        orderService.updateOrderStatus(seller, orderId, newStatus);
    }
}
