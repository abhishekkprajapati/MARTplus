package com.martplus.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing a User in the MARTplus platform.
 * Demonstrates strict OOP Encapsulation with private fields and controlled access.
 */
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password; // BCrypt hashed password
    private String role;     // BUYER, SELLER, ADMIN
    private String status;   // ACTIVE, PENDING_VERIFICATION, INACTIVE
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String firstName, String lastName, String email, String phone,
                String password, String role, String status, boolean emailVerified,
                boolean phoneVerified, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role != null ? role.toUpperCase() : "BUYER";
        this.status = status != null ? status.toUpperCase() : "ACTIVE";
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.toUpperCase() : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
