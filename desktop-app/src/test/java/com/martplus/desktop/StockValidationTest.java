package com.martplus.desktop;

import com.martplus.desktop.dao.CartDao;
import com.martplus.desktop.dao.ProductDao;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.CartItem;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Product Stock & Shopping Cart Business Rules Tests")
public class StockValidationTest {

    private CartDao cartDao;
    private ProductDao productDao;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartDao = Mockito.mock(CartDao.class);
        productDao = Mockito.mock(ProductDao.class);
        cartService = new CartService(cartDao, productDao);
    }

    @Test
    @DisplayName("Should successfully add to cart when stock is sufficient")
    void testAddToCartSuccess() {
        Product product = new Product(10L, "Wireless Mouse", "Ergonomic", BigDecimal.valueOf(499.00),
                20, "Electronics", null, "ACTIVE", 2L, LocalDateTime.now());

        when(productDao.findById(10L)).thenReturn(Optional.of(product));
        when(cartDao.findByBuyerIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cartService.addToCart(1L, 10L, 5));
        verify(cartDao, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should reject adding to cart when requested quantity exceeds available stock")
    void testAddToCartExceedsStock() {
        Product product = new Product(10L, "Wireless Mouse", "Ergonomic", BigDecimal.valueOf(499.00),
                3, "Electronics", null, "ACTIVE", 2L, LocalDateTime.now());

        when(productDao.findById(10L)).thenReturn(Optional.of(product));
        when(cartDao.findByBuyerIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> cartService.addToCart(1L, 10L, 5));
        assertTrue(ex.getMessage().contains("Only 3 units available in stock"));
        verify(cartDao, never()).save(any());
    }

    @Test
    @DisplayName("Should reject inactive product additions")
    void testAddToCartInactiveProduct() {
        Product product = new Product(10L, "Discontinued Item", "Old", BigDecimal.valueOf(100.00),
                50, "General", null, "INACTIVE", 2L, LocalDateTime.now());

        when(productDao.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(ValidationException.class, () -> cartService.addToCart(1L, 10L, 1));
        verify(cartDao, never()).save(any());
    }
}
