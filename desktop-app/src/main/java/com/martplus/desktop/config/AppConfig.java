package com.martplus.desktop.config;

import com.martplus.desktop.model.User;

/**
 * Application Context holding global session state (e.g., current authenticated user).
 */
public class AppConfig {

    private static volatile User currentUser = null;

    private AppConfig() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static void clearSession() {
        currentUser = null;
    }
}
