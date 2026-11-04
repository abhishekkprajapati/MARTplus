package com.martplus.desktop.controller;

import com.martplus.desktop.model.*;
import com.martplus.desktop.service.CartService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.WishlistService;

import java.util.List;

/**
 * Controller mediating Buyer operations between UI and Services.
 */
public class BuyerController {

    private final ProductService productService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final OrderService orderService;

    public BuyerController(ProductService productService, CartService cartService,
                           WishlistService wishlistService, OrderService orderService) {
        this.productService = productService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.orderService = orderService;
    }

    public List<Product> browseProducts(String keyword, String category) {
        return productService.searchProducts(keyword, category);
    }

    public List<CartItem> getCart(Long buyerId) {
        return cartService.getCartItems(buyerId);
    }

    public void addToCart(Long buyerId, Long productId, int quantity) {
        cartService.addToCart(buyerId, productId, quantity);
    }

    public Order checkout(User buyer, Long addressId, String paymentMethod) {
        return orderService.processCheckout(buyer, addressId, paymentMethod);
    }

    public List<Order> getOrders(Long buyerId) {
        return orderService.getOrdersForBuyer(buyerId);
    }
}
