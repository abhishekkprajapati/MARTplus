package com.ShopSphere.controller;

import com.ShopSphere.dto.ActivityLogResponse;
import com.ShopSphere.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activity")
@PreAuthorize("hasRole('ADMIN')")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(
            ActivityLogService activityLogService
    ) {
        this.activityLogService = activityLogService;
    }

    // Get all activity logs
    @GetMapping
    public ResponseEntity<?> getAllLogs() {

        try {

            List<ActivityLogResponse> logs =
                    activityLogService.getAllLogs();

            return ResponseEntity.ok(logs);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get current admin's activity logs
    @GetMapping("/my")
    public ResponseEntity<?> getMyLogs(
            Authentication authentication
    ) {

        try {

            List<ActivityLogResponse> logs =
                    activityLogService.getAdminLogs(
                            authentication.getName()
                    );

            return ResponseEntity.ok(logs);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
