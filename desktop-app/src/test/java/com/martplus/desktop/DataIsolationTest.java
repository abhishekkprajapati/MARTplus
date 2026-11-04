package com.martplus.desktop;

import com.martplus.desktop.dao.CategoryDao;
import com.martplus.desktop.dao.ProductDao;
import com.martplus.desktop.dao.UserDao;
import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Role-Based Access Control & Data Isolation Tests")
public class DataIsolationTest {

    private ProductDao productDao;
    private CategoryDao categoryDao;
    private UserDao userDao;
    private ProductService productService;
    private UserService userService;

    private User seller1;
    private User seller2;
    private User buyer;
    private User admin;
    private Product seller1Product;

    @BeforeEach
    void setUp() {
        productDao = Mockito.mock(ProductDao.class);
        categoryDao = Mockito.mock(CategoryDao.class);
        userDao = Mockito.mock(UserDao.class);

        productService = new ProductService(productDao, categoryDao, null);
        userService = new UserService(userDao, null);

        seller1 = new User(10L, "Seller", "One", "seller1@martplus.com", "9000000001",
                "hash", "SELLER", "ACTIVE", true, true, LocalDateTime.now());

        seller2 = new User(20L, "Seller", "Two", "seller2@martplus.com", "9000000002",
                "hash", "SELLER", "ACTIVE", true, true, LocalDateTime.now());

        buyer = new User(30L, "Buyer", "Bob", "buyer@martplus.com", "9000000003",
                "hash", "BUYER", "ACTIVE", true, true, LocalDateTime.now());

        admin = new User(1L, "Admin", "Super", "admin@martplus.com", "9999999999",
                "hash", "ADMIN", "ACTIVE", true, true, LocalDateTime.now());

        seller1Product = new Product(100L, "Seller 1 Item", "Description", BigDecimal.valueOf(500),
                10, "Electronics", null, "ACTIVE", seller1.getId(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Seller 2 must NOT be allowed to update or delete Seller 1's product")
    void testSellerOwnershipIsolation() {
        when(productDao.findById(100L)).thenReturn(Optional.of(seller1Product));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                productService.updateProduct(seller2, 100L, "Hacked", "Desc", BigDecimal.valueOf(100), 10, "Cat", null)
        );
        assertTrue(ex.getMessage().contains("You do not own this product listing"));
        verify(productDao, never()).update(any());

        assertThrows(AuthenticationException.class, () ->
                productService.deleteProduct(seller2, 100L)
        );
        verify(productDao, never()).deleteById(any());
    }

    @Test
    @DisplayName("Admin CAN update or manage any seller's product")
    void testAdminOversightAllowed() {
        when(productDao.findById(100L)).thenReturn(Optional.of(seller1Product));

        assertDoesNotThrow(() ->
                productService.updateProduct(admin, 100L, "Updated Title", "Desc", BigDecimal.valueOf(550), 15, "Cat", null)
        );
        verify(productDao, times(1)).update(any());
    }

    @Test
    @DisplayName("Non-admin users must be rejected from administrative operations")
    void testNonAdminUserManagementRejected() {
        assertThrows(AuthenticationException.class, () ->
                userService.getAllUsers(buyer)
        );

        assertThrows(AuthenticationException.class, () ->
                userService.updateUserRole(seller1, 20L, "ADMIN")
        );
    }
}
