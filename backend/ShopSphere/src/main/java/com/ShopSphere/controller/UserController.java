package com.ShopSphere.controller;

import com.ShopSphere.entity.User;
import com.ShopSphere.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userService.getUserByEmail(email);

        user.setPassword(null);

        return ResponseEntity.ok(user);
    }
}
