package com.martplus.desktop.ui;

import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.model.User;

/**
 * Factory class for dynamic Dashboard instantiation.
 * Demonstrates:
 * 1. Runtime Polymorphism: Instantiates specific subtype and returns reference to common DashboardView interface.
 * 2. Factory Pattern for role-based GUI orchestration.
 */
public class RoleDashboardFactory {

    public static DashboardView createDashboard(User user, MainWindow mainWindow) {
        if (user == null || user.getRole() == null) {
            throw new AuthenticationException("Cannot instantiate dashboard: User or role is null.");
        }

        String role = user.getRole().trim().toUpperCase();

        return switch (role) {
            case "BUYER" -> new BuyerDashboard(mainWindow, user);
            case "SELLER" -> new SellerDashboard(mainWindow, user);
            case "ADMIN" -> new AdminDashboard(mainWindow, user);
            default -> throw new AuthenticationException("Unrecognized role: " + role);
        };
    }
}
