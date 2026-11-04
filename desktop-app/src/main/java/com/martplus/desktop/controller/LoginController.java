package com.martplus.desktop.controller;

import com.martplus.desktop.model.User;
import com.martplus.desktop.service.AuthenticationService;

/**
 * Controller mediating user login actions between Login UI and AuthenticationService.
 */
public class LoginController {

    private final AuthenticationService authService;

    public LoginController(AuthenticationService authService) {
        this.authService = authService;
    }

    public User handleLogin(String email, String password, String role) {
        return authService.login(email, password, role);
    }

    public void handleLogout() {
        authService.logout();
    }
}
