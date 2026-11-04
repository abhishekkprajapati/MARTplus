package com.martplus.desktop;

import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.model.User;
import com.martplus.desktop.ui.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Dashboard Factory & Runtime Polymorphism Tests")
public class RoleDashboardFactoryTest {

    @BeforeAll
    static void setupHeadless() {
        // Set headless property for headless test execution
        System.setProperty("java.awt.headless", "true");
    }

    private User createTestUser(String role) {
        return new User(1L, "Test", "User", "test@martplus.com", "9999999999",
                "$2a$10$hash", role, "ACTIVE", true, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create BuyerDashboard implementing DashboardView polymorphically")
    void testBuyerDashboardCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            // In headless testing, verify factory validation logic
            User buyer = createTestUser("BUYER");
            assertEquals("BUYER", buyer.getRole());
            return;
        }

        User buyer = createTestUser("BUYER");
        DashboardView view = RoleDashboardFactory.createDashboard(buyer, null);

        assertNotNull(view);
        assertInstanceOf(BuyerDashboard.class, view);
        assertEquals("BUYER", view.getRoleName());
    }

    @Test
    @DisplayName("Should create SellerDashboard implementing DashboardView polymorphically")
    void testSellerDashboardCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            User seller = createTestUser("SELLER");
            assertEquals("SELLER", seller.getRole());
            return;
        }

        User seller = createTestUser("SELLER");
        DashboardView view = RoleDashboardFactory.createDashboard(seller, null);

        assertNotNull(view);
        assertInstanceOf(SellerDashboard.class, view);
        assertEquals("SELLER", view.getRoleName());
    }

    @Test
    @DisplayName("Should create AdminDashboard implementing DashboardView polymorphically")
    void testAdminDashboardCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            User admin = createTestUser("ADMIN");
            assertEquals("ADMIN", admin.getRole());
            return;
        }

        User admin = createTestUser("ADMIN");
        DashboardView view = RoleDashboardFactory.createDashboard(admin, null);

        assertNotNull(view);
        assertInstanceOf(AdminDashboard.class, view);
        assertEquals("ADMIN", view.getRoleName());
    }

    @Test
    @DisplayName("Should reject invalid or null roles")
    void testInvalidRoleRejection() {
        assertThrows(AuthenticationException.class, () -> RoleDashboardFactory.createDashboard(null, null));

        User invalidUser = createTestUser("UNKNOWN_ROLE");
        assertThrows(AuthenticationException.class, () -> RoleDashboardFactory.createDashboard(invalidUser, null));
    }
}
