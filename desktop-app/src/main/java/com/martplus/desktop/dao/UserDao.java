package com.martplus.desktop.dao;

import com.martplus.desktop.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for User accounts.
 */
public interface UserDao extends GenericDao<User, Long> {

    Optional<User> findByEmailAndRole(String email, String role);

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    void updateStatus(Long userId, String status);

    void updateRole(Long userId, String role);
}
