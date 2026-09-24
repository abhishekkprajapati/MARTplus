package com.ShopSphere.repository;

import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySeller(User seller);

    List<Product> findByCategory(String category);

    List<Product> findByStatus(String status);

    List<Product> findBySellerAndStatus(User seller, String status);
}
