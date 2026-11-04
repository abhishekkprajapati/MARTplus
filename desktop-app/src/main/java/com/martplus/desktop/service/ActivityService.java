package com.martplus.desktop.service;

import com.martplus.desktop.dao.ActivityLogDao;
import com.martplus.desktop.model.ActivityLog;

import java.util.List;

/**
 * Service managing audit logs for administrative operations.
 */
public class ActivityService {

    private final ActivityLogDao activityLogDao;

    public ActivityService(ActivityLogDao activityLogDao) {
        this.activityLogDao = activityLogDao;
    }

    public void logActivity(Long adminId, String action, String description) {
        activityLogDao.logActivity(adminId, action, description);
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        return activityLogDao.findRecentLogs(limit);
    }
}
