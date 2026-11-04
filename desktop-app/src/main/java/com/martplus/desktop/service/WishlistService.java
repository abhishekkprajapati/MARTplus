package com.martplus.desktop.service;

import com.martplus.desktop.dao.WishlistDao;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.WishlistItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing Buyer Wishlist operations.
 */
public class WishlistService {

    private final WishlistDao wishlistDao;

    public WishlistService(WishlistDao wishlistDao) {
        this.wishlistDao = wishlistDao;
    }

    public List<WishlistItem> getWishlistItems(Long buyerId) {
        if (buyerId == null) {
            throw new ValidationException("Buyer ID is required");
        }
        return wishlistDao.findByBuyerId(buyerId);
    }

    public void addToWishlist(Long buyerId, Long productId) {
        if (buyerId == null || productId == null) {
            throw new ValidationException("Buyer ID and Product ID are required");
        }
        if (wishlistDao.existsByBuyerIdAndProductId(buyerId, productId)) {
            throw new ValidationException("This product is already in your wishlist.");
        }
        WishlistItem item = new WishlistItem(null, buyerId, productId, LocalDateTime.now());
        wishlistDao.save(item);
    }

    public void removeFromWishlist(Long wishlistItemId) {
        wishlistDao.deleteById(wishlistItemId);
    }

    public void removeByProduct(Long buyerId, Long productId) {
        wishlistDao.deleteByBuyerIdAndProductId(buyerId, productId);
    }
}
