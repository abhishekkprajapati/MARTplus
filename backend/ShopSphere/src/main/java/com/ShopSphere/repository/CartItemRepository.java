package com.ShopSphere.repository;

import com.ShopSphere.entity.CartItem;
import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByBuyer(User buyer);

    Optional<CartItem> findByBuyerAndProduct(
            User buyer,
            Product product
    );

    Optional<CartItem> findByIdAndBuyer(
            Long id,
            User buyer
    );

    void deleteByBuyer(User buyer);

    boolean existsByBuyerAndProduct(
            User buyer,
            Product product
    );
}
