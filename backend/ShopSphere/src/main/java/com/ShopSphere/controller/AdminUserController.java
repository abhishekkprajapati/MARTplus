package com.ShopSphere.controller;

import com.ShopSphere.dto.RegisterRequest;
import com.ShopSphere.entity.User;
import com.ShopSphere.service.ActivityLogService;
import com.ShopSphere.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final ActivityLogService activityLogService;

    public AdminUserController(
            UserService userService,
            ActivityLogService activityLogService
    ) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody RegisterRequest request,
            Authentication authentication
    ) {
        try {

            if (request.getRole() == null ||
                    (!request.getRole().equalsIgnoreCase("BUYER")
                    && !request.getRole().equalsIgnoreCase("SELLER"))) {

                return ResponseEntity.badRequest()
                        .body("Only BUYER or SELLER can be created");
            }

            User user = new User();

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(request.getPassword());
            user.setRole(request.getRole().toUpperCase());

            User savedUser =
                    userService.createAdminManagedUser(user);

            activityLogService.createLog(
                    authentication.getName(),
                    "USER_CREATED",
                    "Created user: " + savedUser.getEmail()
            );

            savedUser.setPassword(null);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get all users
    @GetMapping
    public ResponseEntity<?> getAllUsers() {

        try {

            List<User> users = userService.getAllUsers();

            users.forEach(user ->
                    user.setPassword(null)
            );

            return ResponseEntity.ok(users);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id
    ) {

        try {

            User user = userService.getUserById(id);

            user.setPassword(null);

            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            Authentication authentication
    ) {

        try {

            User user =
                    userService.updateUser(id, updatedUser);

            activityLogService.createLog(
                    authentication.getName(),
                    "USER_UPDATED",
                    "Updated user: " + user.getEmail()
            );

            user.setPassword(null);

            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Deactivate user
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            User user =
                    userService.deactivateUser(id);

            activityLogService.createLog(
                    authentication.getName(),
                    "USER_DEACTIVATED",
                    "Deactivated user: " + user.getEmail()
            );

            user.setPassword(null);

            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}