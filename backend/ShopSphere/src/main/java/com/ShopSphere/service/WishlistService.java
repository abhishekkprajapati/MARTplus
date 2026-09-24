package com.ShopSphere.service;

import com.ShopSphere.dto.WishlistItemResponse;
import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.entity.WishlistItem;
import com.ShopSphere.repository.ProductRepository;
import com.ShopSphere.repository.UserRepository;
import com.ShopSphere.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getBuyer(String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (!"BUYER".equalsIgnoreCase(buyer.getRole())) {
            throw new RuntimeException("Only buyers can use wishlist");
        }

        return buyer;
    }

    public List<WishlistItemResponse> getWishlist(String buyerEmail) {
        User buyer = getBuyer(buyerEmail);

        return wishlistItemRepository.findByBuyer(buyer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WishlistItemResponse addToWishlist(
            String buyerEmail,
            Long productId
    ) {
        User buyer = getBuyer(buyerEmail);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("Product is not active");
        }

        boolean alreadyExists =
                wishlistItemRepository.existsByBuyerAndProduct(
                        buyer,
                        product
                );

        if (alreadyExists) {
            throw new RuntimeException(
                    "Product is already in wishlist"
            );
        }

        WishlistItem wishlistItem = new WishlistItem();

        wishlistItem.setBuyer(buyer);
        wishlistItem.setProduct(product);

        WishlistItem savedItem =
                wishlistItemRepository.save(wishlistItem);

        return toResponse(savedItem);
    }

    public void removeFromWishlist(
            String buyerEmail,
            Long productId
    ) {
        User buyer = getBuyer(buyerEmail);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        WishlistItem wishlistItem =
                wishlistItemRepository
                        .findByBuyerAndProduct(buyer, product)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product is not in wishlist"
                                )
                        );

        wishlistItemRepository.delete(wishlistItem);
    }

    public void clearWishlist(String buyerEmail) {
        User buyer = getBuyer(buyerEmail);

        List<WishlistItem> wishlistItems =
                wishlistItemRepository.findByBuyer(buyer);

        wishlistItemRepository.deleteAll(wishlistItems);
    }

    private WishlistItemResponse toResponse(
            WishlistItem wishlistItem
    ) {
        Product product = wishlistItem.getProduct();

        return new WishlistItemResponse(
                wishlistItem.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl(),
                wishlistItem.getCreatedAt()
        );
    }
}