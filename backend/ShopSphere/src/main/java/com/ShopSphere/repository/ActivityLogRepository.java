package com.ShopSphere.repository;

import com.ShopSphere.entity.ActivityLog;
import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    List<ActivityLog> findByAdminOrderByCreatedAtDesc(User admin);
}
