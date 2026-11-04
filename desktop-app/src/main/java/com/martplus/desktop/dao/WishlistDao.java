package com.martplus.desktop.dao;

import com.martplus.desktop.model.WishlistItem;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for Wishlist operations.
 */
public interface WishlistDao extends GenericDao<WishlistItem, Long> {

    List<WishlistItem> findByBuyerId(Long buyerId);

    boolean existsByBuyerIdAndProductId(Long buyerId, Long productId);

    void deleteByBuyerIdAndProductId(Long buyerId, Long productId);
}
