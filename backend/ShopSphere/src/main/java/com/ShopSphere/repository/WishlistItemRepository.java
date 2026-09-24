package com.ShopSphere.repository;

import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByBuyer(User buyer);

    Optional<WishlistItem> findByBuyerAndProduct(
            User buyer,
            Product product
    );

    Optional<WishlistItem> findByIdAndBuyer(
            Long id,
            User buyer
    );

    boolean existsByBuyerAndProduct(
            User buyer,
            Product product
    );

    void deleteByBuyerAndProduct(
            User buyer,
            Product product
    );
}