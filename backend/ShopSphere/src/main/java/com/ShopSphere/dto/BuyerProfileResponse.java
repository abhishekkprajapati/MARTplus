package com.ShopSphere.dto;

import java.time.LocalDateTime;

public class BuyerProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public BuyerProfileResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String role,
            String status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}