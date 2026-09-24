package com.ShopSphere.service;

import com.ShopSphere.dto.ActivityLogResponse;
import com.ShopSphere.entity.ActivityLog;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.ActivityLogRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            UserRepository userRepository
    ) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    public ActivityLog createLog(
            String adminEmail,
            String action,
            String description
    ) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException(
                    "Only admins can create activity logs"
            );
        }

        ActivityLog log = new ActivityLog();

        log.setAdmin(admin);
        log.setAction(action);
        log.setDescription(description);

        return activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getAllLogs() {

        return activityLogRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ActivityLogResponse> getAdminLogs(
            String adminEmail
    ) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        return activityLogRepository
                .findByAdminOrderByCreatedAtDesc(admin)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ActivityLogResponse toResponse(ActivityLog log) {

        User admin = log.getAdmin();

        return new ActivityLogResponse(
                log.getId(),
                admin.getId(),
                admin.getEmail(),
                log.getAction(),
                log.getDescription(),
                log.getCreatedAt()
        );
    }
}
