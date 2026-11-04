package com.martplus.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing an Administrative Activity Log record.
 */
public class ActivityLog {
    private Long id;
    private Long adminId;
    private String adminName;  // Joined admin user name
    private String action;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLog() {
    }

    public ActivityLog(Long id, Long adminId, String action, String description, LocalDateTime createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
