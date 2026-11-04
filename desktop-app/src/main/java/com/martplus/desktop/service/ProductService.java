package com.martplus.desktop.service;

import com.martplus.desktop.dao.CategoryDao;
import com.martplus.desktop.dao.ProductDao;
import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.Category;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing Product catalog operations, categories, and inventory access.
 * Demonstrates Collections (List, Set, Map) and Generics:
 * - List<Product> for product filtering and ordering.
 * - Set<String> for extracting distinct category names.
 * - Map<Long, Product> for O(1) in-memory fast product lookups.
 */
public class ProductService {

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final ActivityService activityService;

    public ProductService(ProductDao productDao, CategoryDao categoryDao, ActivityService activityService) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
        this.activityService = activityService;
    }

    public List<Product> getAvailableProducts() {
        return productDao.findAvailableProducts();
    }

    public Optional<Product> getProductById(Long id) {
        return productDao.findById(id);
    }

    public List<Product> searchProducts(String keyword, String category) {
        return productDao.searchByNameAndCategory(keyword, category);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        if (sellerId == null) {
            return Collections.emptyList();
        }
        return productDao.findBySellerId(sellerId);
    }

    public List<Product> getAllProductsForAdmin(User adminUser) {
        enforceAdmin(adminUser);
        return productDao.findAll();
    }

    /**
     * Demonstrates Set<String> collection: collects distinct non-null categories.
     */
    public Set<String> getUniqueCategoryNames() {
        List<Category> categories = categoryDao.findActiveCategories();
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.add("All");
        for (Category c : categories) {
            if (c.getName() != null && !c.getName().isBlank()) {
                set.add(c.getName().trim());
            }
        }
        return set;
    }

    /**
     * Demonstrates Map<Long, Product> collection: returns a map of product IDs to products.
     */
    public Map<Long, Product> getProductMap(List<Long> productIds) {
        Map<Long, Product> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return map;
        }
        for (Long id : productIds) {
            productDao.findById(id).ifPresent(p -> map.put(p.getId(), p));
        }
        return map;
    }

    public Product createProduct(User seller, String name, String description,
                                 BigDecimal price, Integer stock, String category, String imageUrl) {
        enforceSellerOrAdmin(seller);

        ValidationUtil.requireNonEmpty(name, "Product name");
        ValidationUtil.validatePositivePrice(price, "Product price");
        ValidationUtil.validateNonNegativeStock(stock, "Stock quantity");

        Product product = new Product(
                null,
                name.trim(),
                description != null ? description.trim() : "",
                price,
                stock,
                category != null ? category.trim() : "General",
                imageUrl != null ? imageUrl.trim() : "",
                "ACTIVE",
                seller.getId(),
                LocalDateTime.now()
        );

        return productDao.save(product);
    }

    public void updateProduct(User currentUser, Long productId, String name, String description,
                              BigDecimal price, Integer stock, String category, String imageUrl) {
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found"));

        validateProductOwnership(currentUser, existing);

        ValidationUtil.requireNonEmpty(name, "Product name");
        ValidationUtil.validatePositivePrice(price, "Product price");
        ValidationUtil.validateNonNegativeStock(stock, "Stock quantity");

        existing.setName(name.trim());
        existing.setDescription(description != null ? description.trim() : "");
        existing.setPrice(price);
        existing.setStock(stock);
        existing.setCategory(category != null ? category.trim() : "General");
        existing.setImageUrl(imageUrl != null ? imageUrl.trim() : "");

        productDao.update(existing);
    }

    public void toggleProductStatus(User currentUser, Long productId, String newStatus) {
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found"));

        validateProductOwnership(currentUser, existing);
        productDao.updateStatus(productId, newStatus.toUpperCase());
    }

    public void deleteProduct(User currentUser, Long productId) {
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found"));

        validateProductOwnership(currentUser, existing);
        productDao.deleteById(productId);
    }

    private void validateProductOwnership(User user, Product product) {
        if (user == null) {
            throw new AuthenticationException("Authentication required");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return; // Admin can manage all products
        }
        if (!"SELLER".equalsIgnoreCase(user.getRole()) || !user.getId().equals(product.getSellerId())) {
            throw new AuthenticationException("Access denied: You do not own this product listing.");
        }
    }

    private void enforceAdmin(User user) {
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access denied: Administrative privileges required.");
        }
    }

    private void enforceSellerOrAdmin(User user) {
        if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            throw new AuthenticationException("Access denied: Only sellers or administrators can manage products.");
        }
    }
}
