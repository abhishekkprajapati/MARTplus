package com.ShopSphere.service;

import com.ShopSphere.dto.CartItemResponse;
import com.ShopSphere.entity.CartItem;
import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.CartItemRepository;
import com.ShopSphere.repository.ProductRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getBuyer(String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (!"BUYER".equalsIgnoreCase(buyer.getRole())) {
            throw new RuntimeException("Only buyers can use cart");
        }

        return buyer;
    }

    public List<CartItemResponse> getCart(String buyerEmail) {
        User buyer = getBuyer(buyerEmail);

        return cartItemRepository.findByBuyer(buyer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CartItemResponse addToCart(
            String buyerEmail,
            Long productId,
            Integer quantity
    ) {
        User buyer = getBuyer(buyerEmail);

        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("Product is not active");
        }

        if (product.getStock() == null || product.getStock() < 1) {
            throw new RuntimeException("Product is out of stock");
        }

        CartItem cartItem = cartItemRepository
                .findByBuyerAndProduct(buyer, product)
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new RuntimeException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {
            if (quantity > product.getStock()) {
                throw new RuntimeException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem = new CartItem();
            cartItem.setBuyer(buyer);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        }

        CartItem savedItem = cartItemRepository.save(cartItem);

        return toResponse(savedItem);
    }

    public CartItemResponse updateQuantity(
            String buyerEmail,
            Long cartItemId,
            Integer quantity
    ) {
        User buyer = getBuyer(buyerEmail);

        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        CartItem cartItem = cartItemRepository
                .findByIdAndBuyer(cartItemId, buyer)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Product product = cartItem.getProduct();

        if (product.getStock() == null || quantity > product.getStock()) {
            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }

        cartItem.setQuantity(quantity);

        CartItem savedItem = cartItemRepository.save(cartItem);

        return toResponse(savedItem);
    }

    public void removeFromCart(
            String buyerEmail,
            Long cartItemId
    ) {
        User buyer = getBuyer(buyerEmail);

        CartItem cartItem = cartItemRepository
                .findByIdAndBuyer(cartItemId, buyer)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String buyerEmail) {
        User buyer = getBuyer(buyerEmail);
        cartItemRepository.deleteByBuyer(buyer);
    }

    private CartItemResponse toResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal,
                product.getImageUrl()
        );
    }
}