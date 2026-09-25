package com.ShopSphere.repository;

import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRole(String email, String role);

    List<User> findAllByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByPhoneAndRole(String phone, String role);

    boolean existsByEmail(String email);

    boolean existsByEmailAndRole(String email, String role);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndRole(String phone, String role);

    long countByRoleIgnoreCase(String role);
}