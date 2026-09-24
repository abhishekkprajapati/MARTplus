package com.ShopSphere.repository;

import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByTransactionId(String transactionId);
}