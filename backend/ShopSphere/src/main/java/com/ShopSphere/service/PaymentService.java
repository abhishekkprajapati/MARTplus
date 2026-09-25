package com.ShopSphere.service;

import com.ShopSphere.dto.PaymentResponse;
import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.Payment;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.OrderRepository;
import com.ShopSphere.repository.PaymentRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    private User getBuyer(String email) {
        return userRepository.findByEmailAndRole(email, "BUYER")
                .orElseThrow(() ->
                        new RuntimeException("Buyer not found")
                );
    }

    @Transactional
    public PaymentResponse createPayment(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException(
                    "Payment cannot be created for a cancelled order"
            );
        }

        Payment existingPayment =
                paymentRepository.findByOrder(order)
                        .orElse(null);

        if (existingPayment != null) {
            return toResponse(existingPayment);
        }

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(order.getPaymentMethod());
        payment.setStatus("PENDING");

        Payment savedPayment =
                paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse markPaymentSuccessful(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        Payment payment =
                paymentRepository.findByOrder(order)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException(
                    "Cancelled order cannot be paid"
            );
        }

        payment.setStatus("SUCCESS");

        if (payment.getTransactionId() == null) {
            payment.setTransactionId(
                    "TXN-" + UUID.randomUUID()
            );
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        order.setPaymentStatus("PAID");
        orderRepository.save(order);

        return toResponse(savedPayment);
    }

    public PaymentResponse getPayment(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        Payment payment =
                paymentRepository.findByOrder(order)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}