package com.martplus.desktop;

import com.martplus.desktop.config.DatabaseConfig;
import com.martplus.desktop.dao.*;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.*;
import com.martplus.desktop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("JDBC Transaction Management & Checkout Tests")
public class CheckoutTransactionTest {

    private OrderDao orderDao;
    private CartDao cartDao;
    private ProductDao productDao;
    private AddressDao addressDao;
    private InventoryDao inventoryDao;
    private UserDao userDao;
    private OrderService orderService;

    private Connection proxyConn;
    private AtomicInteger setAutoCommitCount;
    private AtomicInteger commitCount;
    private AtomicInteger rollbackCount;
    private AtomicInteger closeCount;

    private User buyer;
    private Address address;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        orderDao = Mockito.mock(OrderDao.class);
        cartDao = Mockito.mock(CartDao.class);
        productDao = Mockito.mock(ProductDao.class);
        addressDao = Mockito.mock(AddressDao.class);
        inventoryDao = Mockito.mock(InventoryDao.class);
        userDao = Mockito.mock(UserDao.class);

        orderService = new OrderService(orderDao, cartDao, productDao, addressDao, inventoryDao, userDao, null);

        setAutoCommitCount = new AtomicInteger(0);
        commitCount = new AtomicInteger(0);
        rollbackCount = new AtomicInteger(0);
        closeCount = new AtomicInteger(0);

        // Safe Dynamic Proxy for java.sql.Connection (independent of Java version agent restrictions)
        proxyConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setAutoCommit".equals(name)) {
                        setAutoCommitCount.incrementAndGet();
                        return null;
                    }
                    if ("commit".equals(name)) {
                        commitCount.incrementAndGet();
                        return null;
                    }
                    if ("rollback".equals(name)) {
                        rollbackCount.incrementAndGet();
                        return null;
                    }
                    if ("close".equals(name)) {
                        closeCount.incrementAndGet();
                        return null;
                    }
                    return null;
                }
        );

        buyer = new User(1L, "Alice", "Buyer", "buyer@martplus.com", "9876543210",
                "hash", "BUYER", "ACTIVE", true, true, LocalDateTime.now());

        address = new Address(5L, 1L, "Alice Buyer", "9876543210",
                "123 Main St", "Metropolis", "NY", "10001", true, LocalDateTime.now(), LocalDateTime.now());

        product = new Product(10L, "Premium Headphones", "Noise cancelling",
                BigDecimal.valueOf(2500.00), 10, "Audio", null, "ACTIVE", 2L, LocalDateTime.now());

        cartItem = new CartItem(100L, 1L, 10L, 2, LocalDateTime.now(), LocalDateTime.now());
        cartItem.setProduct(product);
    }

    @Test
    @DisplayName("Should commit transaction when all checkout steps succeed")
    void testCheckoutTransactionCommit() {
        orderService.setConnectionSupplier(() -> proxyConn);

        when(addressDao.findByIdAndBuyerId(5L, 1L)).thenReturn(Optional.of(address));
        when(cartDao.findByBuyerId(1L)).thenReturn(List.of(cartItem));
        when(productDao.lockProductForUpdate(eq(proxyConn), eq(10L))).thenReturn(Optional.of(product));

        Order createdOrder = new Order(500L, 1L, 5L, BigDecimal.valueOf(5000.00),
                "PLACED", "PENDING", "COD", LocalDateTime.now(), LocalDateTime.now());
        when(orderDao.insertOrder(eq(proxyConn), any(Order.class))).thenReturn(createdOrder);

        Order result = orderService.processCheckout(buyer, 5L, "COD");

        assertNotNull(result);
        assertEquals(500L, result.getId());

        // Verify atomic transaction sequence
        assertTrue(setAutoCommitCount.get() >= 1, "setAutoCommit(false) must be called");
        verify(productDao, times(1)).lockProductForUpdate(proxyConn, 10L);
        verify(orderDao, times(1)).insertOrder(eq(proxyConn), any(Order.class));
        verify(orderDao, times(1)).insertOrderItems(eq(proxyConn), anyList());
        verify(inventoryDao, times(1)).deductStock(proxyConn, 10L, 2);
        verify(cartDao, times(1)).deleteByBuyerId(proxyConn, 1L);
        verify(orderDao, times(1)).insertTracking(eq(proxyConn), any(OrderTracking.class));

        assertEquals(1, commitCount.get(), "Transaction commit() must be called exactly once");
        assertEquals(0, rollbackCount.get(), "Transaction rollback() must NOT be called on success");
        assertEquals(1, closeCount.get(), "Connection close() must be called in finally block");
    }

    @Test
    @DisplayName("Should rollback transaction when product stock is insufficient")
    void testCheckoutTransactionRollbackOnStockShortage() {
        orderService.setConnectionSupplier(() -> proxyConn);

        when(addressDao.findByIdAndBuyerId(5L, 1L)).thenReturn(Optional.of(address));
        when(cartDao.findByBuyerId(1L)).thenReturn(List.of(cartItem));

        // Product has only 1 in stock, but cart requests 2
        Product lowStockProduct = new Product(10L, "Premium Headphones", "Noise cancelling",
                BigDecimal.valueOf(2500.00), 1, "Audio", null, "ACTIVE", 2L, LocalDateTime.now());
        when(productDao.lockProductForUpdate(eq(proxyConn), eq(10L))).thenReturn(Optional.of(lowStockProduct));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> orderService.processCheckout(buyer, 5L, "COD"));

        assertTrue(ex.getMessage().contains("Insufficient stock"));

        // Verify rollback occurred and no order items were committed
        assertEquals(1, rollbackCount.get(), "Transaction rollback() must be called upon failure");
        assertEquals(0, commitCount.get(), "Transaction commit() must NOT be called upon failure");
        verify(orderDao, never()).insertOrder(any(), any());
        assertEquals(1, closeCount.get(), "Connection must be closed even after failure");
    }

    @Test
    @DisplayName("Should reject checkout if shopping cart is empty")
    void testCheckoutEmptyCart() {
        when(addressDao.findByIdAndBuyerId(5L, 1L)).thenReturn(Optional.of(address));
        when(cartDao.findByBuyerId(1L)).thenReturn(Collections.emptyList());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> orderService.processCheckout(buyer, 5L, "COD"));
        assertEquals("Your shopping cart is empty.", ex.getMessage());
    }
}
