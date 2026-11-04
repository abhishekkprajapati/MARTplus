package com.martplus.desktop.dao;

import com.martplus.desktop.model.ActivityLog;

import java.util.List;

/**
 * Data Access Object contract for Admin Activity Logs.
 */
public interface ActivityLogDao extends GenericDao<ActivityLog, Long> {

    List<ActivityLog> findRecentLogs(int limit);

    void logActivity(Long adminId, String action, String description);
}
