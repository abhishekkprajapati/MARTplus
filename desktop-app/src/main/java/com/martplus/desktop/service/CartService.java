package com.martplus.desktop.service;

import com.martplus.desktop.dao.CartDao;
import com.martplus.desktop.dao.ProductDao;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.CartItem;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing Buyer Shopping Cart operations.
 */
public class CartService {

    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public List<CartItem> getCartItems(Long buyerId) {
        if (buyerId == null) {
            throw new ValidationException("Buyer ID cannot be null");
        }
        return cartDao.findByBuyerId(buyerId);
    }

    public void addToCart(Long buyerId, Long productId, int quantity) {
        ValidationUtil.validatePositiveQuantity(quantity, "Quantity");

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found"));

        if (!product.isAvailable()) {
            throw new ValidationException("This product is currently unavailable.");
        }

        // Check if item is already in cart
        Optional<CartItem> existingOpt = cartDao.findByBuyerIdAndProductId(buyerId, productId);
        int currentQuantityInCart = existingOpt.map(CartItem::getQuantity).orElse(0);
        int requestedTotalQuantity = currentQuantityInCart + quantity;

        if (requestedTotalQuantity > product.getStock()) {
            throw new ValidationException("Cannot add " + quantity + " more. Only " +
                    product.getStock() + " units available in stock (you have " + currentQuantityInCart + " in cart).");
        }

        if (existingOpt.isPresent()) {
            CartItem item = existingOpt.get();
            cartDao.updateQuantity(item.getId(), requestedTotalQuantity);
        } else {
            CartItem newItem = new CartItem(
                    null,
                    buyerId,
                    productId,
                    quantity,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            cartDao.save(newItem);
        }
    }

    public void updateQuantity(Long cartItemId, int newQuantity) {
        ValidationUtil.validatePositiveQuantity(newQuantity, "Quantity");

        CartItem item = cartDao.findById(cartItemId)
                .orElseThrow(() -> new ValidationException("Cart item not found"));

        Product product = productDao.findById(item.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found"));

        if (newQuantity > product.getStock()) {
            throw new ValidationException("Requested quantity exceeds available stock (" + product.getStock() + ").");
        }

        cartDao.updateQuantity(cartItemId, newQuantity);
    }

    public void removeFromCart(Long cartItemId) {
        cartDao.deleteById(cartItemId);
    }

    public void clearCart(Long buyerId) {
        cartDao.deleteByBuyerId(buyerId);
    }

    public BigDecimal calculateCartTotal(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
}
