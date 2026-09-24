package com.ShopSphere.controller;

import com.ShopSphere.dto.BuyerProfileResponse;
import com.ShopSphere.dto.BuyerProfileUpdateRequest;
import com.ShopSphere.dto.ChangePasswordRequest;
import com.ShopSphere.entity.User;
import com.ShopSphere.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
// import com.ShopSphere.dto.ChangePasswordRequest;

@RestController
@RequestMapping("/api/buyer/profile")
public class BuyerProfileController {

    private final UserService userService;

    public BuyerProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {

        try {
            String email = authentication.getName();

            User user = userService.getUserByEmail(email);

            BuyerProfileResponse response = new BuyerProfileResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody BuyerProfileUpdateRequest request,
            Authentication authentication
    ) {

        try {
            String email = authentication.getName();

            User user = userService.updateBuyerProfile(
                    email,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhone()
            );

            BuyerProfileResponse response = new BuyerProfileResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
public ResponseEntity<?> changePassword(
        @RequestBody ChangePasswordRequest request,
        Authentication authentication
) {

    try {
        String email = authentication.getName();

        userService.changePassword(
                email,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Password changed successfully"
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(e.getMessage());
    }
}

}
