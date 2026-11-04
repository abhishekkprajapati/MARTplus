package com.martplus.desktop.service;

import com.martplus.desktop.dao.UserDao;
import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.User;
import com.martplus.desktop.util.PasswordUtil;
import com.martplus.desktop.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing user profiles, admin user administration, and role-based access rules.
 */
public class UserService {

    private final UserDao userDao;
    private final ActivityService activityService;

    public UserService(UserDao userDao, ActivityService activityService) {
        this.userDao = userDao;
        this.activityService = activityService;
    }

    public List<User> getAllUsers(User adminUser) {
        enforceAdminRole(adminUser);
        return userDao.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    public List<User> getUsersByRole(User adminUser, String role) {
        enforceAdminRole(adminUser);
        if (role == null || role.isBlank() || role.equalsIgnoreCase("ALL")) {
            return userDao.findAll();
        }
        return userDao.findByRole(role);
    }

    public User createUser(User adminUser, String firstName, String lastName, String email,
                           String phone, String rawPassword, String role) {
        enforceAdminRole(adminUser);

        ValidationUtil.requireNonEmpty(firstName, "First name");
        ValidationUtil.requireNonEmpty(lastName, "Last name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonEmpty(rawPassword, "Password");
        ValidationUtil.requireNonEmpty(role, "Role");

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedRole = role.trim().toUpperCase();

        if (userDao.findByEmailAndRole(normalizedEmail, normalizedRole).isPresent()) {
            throw new ValidationException("A user with this email and role already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        User user = new User(
                null,
                firstName.trim(),
                lastName.trim(),
                normalizedEmail,
                phone != null ? phone.trim() : null,
                hashedPassword,
                normalizedRole,
                "ACTIVE",
                true,
                true,
                LocalDateTime.now()
        );

        User saved = userDao.save(user);
        if (activityService != null && adminUser != null) {
            activityService.logActivity(adminUser.getId(), "CREATE_USER",
                    "Created " + saved.getRole() + " user: " + saved.getEmail());
        }
        return saved;
    }

    public void updateUserStatus(User adminUser, Long targetUserId, String newStatus) {
        enforceAdminRole(adminUser);
        ValidationUtil.requireNonEmpty(newStatus, "Status");

        User target = userDao.findById(targetUserId)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (target.getId().equals(adminUser.getId()) && !"ACTIVE".equalsIgnoreCase(newStatus)) {
            throw new ValidationException("Administrators cannot deactivate their own account.");
        }

        userDao.updateStatus(targetUserId, newStatus.toUpperCase());
        if (activityService != null) {
            activityService.logActivity(adminUser.getId(), "UPDATE_USER_STATUS",
                    "Changed status of user " + target.getEmail() + " to " + newStatus.toUpperCase());
        }
    }

    public void updateUserRole(User adminUser, Long targetUserId, String newRole) {
        enforceAdminRole(adminUser);
        ValidationUtil.requireNonEmpty(newRole, "Role");

        User target = userDao.findById(targetUserId)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (target.getId().equals(adminUser.getId())) {
            throw new ValidationException("Administrators cannot change their own role.");
        }

        userDao.updateRole(targetUserId, newRole.toUpperCase());
        if (activityService != null) {
            activityService.logActivity(adminUser.getId(), "UPDATE_USER_ROLE",
                    "Changed role of user " + target.getEmail() + " to " + newRole.toUpperCase());
        }
    }

    public void updateProfile(User currentUser, String firstName, String lastName, String phone) {
        if (currentUser == null) {
            throw new AuthenticationException("User is not authenticated");
        }
        ValidationUtil.requireNonEmpty(firstName, "First name");
        ValidationUtil.requireNonEmpty(lastName, "Last name");
        ValidationUtil.validatePhone(phone);

        currentUser.setFirstName(firstName.trim());
        currentUser.setLastName(lastName.trim());
        currentUser.setPhone(phone != null ? phone.trim() : null);

        userDao.update(currentUser);
    }

    private void enforceAdminRole(User user) {
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access denied: Administrative privileges required.");
        }
    }
}
