package com.ShopSphere.dto;

import java.time.LocalDateTime;

public class ActivityLogResponse {

    private Long id;
    private Long adminId;
    private String adminEmail;
    private String action;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLogResponse(
            Long id,
            Long adminId,
            String adminEmail,
            String action,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.action = action;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
