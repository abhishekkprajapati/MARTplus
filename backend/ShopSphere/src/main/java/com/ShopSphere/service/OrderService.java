package com.ShopSphere.service;

import com.ShopSphere.dto.AdminDashboardResponse;
import com.ShopSphere.dto.BuyerDashboardResponse;
import com.ShopSphere.dto.OrderItemResponse;
import com.ShopSphere.dto.OrderResponse;
import com.ShopSphere.dto.SellerAnalyticsResponse;
import com.ShopSphere.dto.SellerOrderResponse;
import com.ShopSphere.entity.Address;
import com.ShopSphere.entity.CartItem;
import com.ShopSphere.entity.Order;
import com.ShopSphere.entity.OrderItem;
import com.ShopSphere.entity.OrderTracking;
import com.ShopSphere.entity.Product;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.AddressRepository;
import com.ShopSphere.repository.CartItemRepository;
import com.ShopSphere.repository.OrderItemRepository;
import com.ShopSphere.repository.OrderRepository;
import com.ShopSphere.repository.OrderTrackingRepository;
import com.ShopSphere.repository.ProductRepository;
import com.ShopSphere.repository.UserRepository;
import com.ShopSphere.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository,
            UserRepository userRepository,
            WishlistItemRepository wishlistItemRepository,
            OrderTrackingRepository orderTrackingRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.orderTrackingRepository = orderTrackingRepository;
    }

    private User getBuyer(String email) {

        return userRepository.findByEmailAndRole(email, "BUYER")
                .orElseThrow(() ->
                        new RuntimeException("Buyer not found")
                );
    }

    private User getSeller(String email) {

        return userRepository.findByEmailAndRole(email, "SELLER")
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );
    }

    // ==========================================
    // CANCEL ORDER
    // ==========================================

    @Transactional
    public OrderResponse cancelOrder(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if (!"PLACED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException(
                    "Only placed orders can be cancelled"
            );
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        for (OrderItem orderItem : orderItems) {

            Product product = orderItem.getProduct();

            product.setStock(
                    product.getStock()
                            + orderItem.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus("CANCELLED");

        Order savedOrder =
                orderRepository.save(order);

        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(savedOrder);
        tracking.setStatus("CANCELLED");
        tracking.setDescription("Order cancelled by buyer");
        orderTrackingRepository.save(tracking);

        return toResponse(savedOrder);
    }

    // ==========================================
    // PLACE ORDER
    // ==========================================

    @Transactional
    public OrderResponse placeOrder(
            String buyerEmail,
            Long addressId,
            String paymentMethod
    ) {

        User buyer = getBuyer(buyerEmail);

        if (paymentMethod == null ||
                paymentMethod.isBlank()) {

            throw new RuntimeException(
                    "Payment method is required"
            );
        }

        Address address = addressRepository
                .findByIdAndBuyer(addressId, buyer)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipping address not found"
                        )
                );

        List<CartItem> cartItems =
                cartItemRepository.findByBuyer(buyer);

        if (cartItems.isEmpty()) {
            throw new RuntimeException(
                    "Cart is empty"
            );
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!"ACTIVE".equalsIgnoreCase(
                    product.getStatus())) {

                throw new RuntimeException(
                        "Product is no longer available: "
                                + product.getName()
                );
            }

            if (cartItem.getQuantity() >
                    product.getStock()) {

                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            BigDecimal itemTotal =
                    product.getPrice().multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            totalAmount =
                    totalAmount.add(itemTotal);
        }

        Order order = new Order();

        order.setBuyer(buyer);
        order.setShippingAddress(address);
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod(
                paymentMethod.toUpperCase()
        );

        Order savedOrder =
                orderRepository.save(order);

        List<OrderItem> orderItems =
                new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(
                    cartItem.getQuantity()
            );
            orderItem.setPrice(
                    product.getPrice()
            );

            OrderItem savedOrderItem =
                    orderItemRepository.save(orderItem);

            orderItems.add(savedOrderItem);

            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        cartItemRepository.deleteByBuyer(buyer);

        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(savedOrder);
        tracking.setStatus("PLACED");
        tracking.setDescription("Order placed successfully");
        orderTrackingRepository.save(tracking);

        return toResponse(savedOrder, orderItems);
    }

    // ==========================================
    // BUYER ORDERS
    // ==========================================

    public List<OrderResponse> getMyOrders(
            String buyerEmail
    ) {

        User buyer = getBuyer(buyerEmail);

        return orderRepository
                .findByBuyerOrderByCreatedAtDesc(buyer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // BUYER DASHBOARD
    // ==========================================

    public BuyerDashboardResponse getBuyerDashboard(
            String buyerEmail
    ) {

        User buyer = getBuyer(buyerEmail);

        List<Order> orders =
                orderRepository.findByBuyerOrderByCreatedAtDesc(
                        buyer
                );

        long totalOrders = orders.size();

        long pendingOrders = orders.stream()
                .filter(order -> {

                    String status = order.getStatus();

                    return "PLACED".equalsIgnoreCase(status)
                            || "CONFIRMED".equalsIgnoreCase(status)
                            || "PROCESSING".equalsIgnoreCase(status)
                            || "SHIPPED".equalsIgnoreCase(status);
                })
                .count();

        long deliveredOrders = orders.stream()
                .filter(order ->
                        "DELIVERED".equalsIgnoreCase(
                                order.getStatus()
                        )
                )
                .count();

        long cancelledOrders = orders.stream()
                .filter(order ->
                        "CANCELLED".equalsIgnoreCase(
                                order.getStatus()
                        )
                )
                .count();

        BigDecimal totalSpent = orders.stream()
                .filter(order ->
                        !"CANCELLED".equalsIgnoreCase(
                                order.getStatus()
                        )
                )
                .map(Order::getTotalAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        long cartItems = cartItemRepository
                .findByBuyer(buyer)
                .stream()
                .mapToLong(CartItem::getQuantity)
                .sum();

        long wishlistItems = wishlistItemRepository
                .findByBuyer(buyer)
                .size();

        return new BuyerDashboardResponse(
                totalOrders,
                pendingOrders,
                deliveredOrders,
                cancelledOrders,
                wishlistItems,
                cartItems,
                totalSpent
        );
    }

    public OrderResponse getOrderById(
            String buyerEmail,
            Long orderId
    ) {

        User buyer = getBuyer(buyerEmail);

        Order order = orderRepository
                .findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        return toResponse(order);
    }

    // ==========================================
    // SELLER ORDERS
    // ==========================================

    public List<SellerOrderResponse> getSellerOrders(
            String sellerEmail
    ) {

        User seller = getSeller(sellerEmail);

        List<OrderItem> orderItems =
                orderItemRepository.findByProductSellerId(
                        seller.getId()
                );

        return orderItems.stream()
                .sorted((item1, item2) ->
                        item2.getOrder()
                                .getCreatedAt()
                                .compareTo(
                                        item1.getOrder()
                                                .getCreatedAt()
                                )
                )
                .map(this::toSellerOrderResponse)
                .toList();
    }

    private SellerOrderResponse toSellerOrderResponse(
            OrderItem orderItem
    ) {

        Order order = orderItem.getOrder();
        Product product = orderItem.getProduct();
        User buyer = order.getBuyer();

        String buyerName =
                (
                        (buyer.getFirstName() != null
                                ? buyer.getFirstName()
                                : "")
                                + " "
                                + (buyer.getLastName() != null
                                ? buyer.getLastName()
                                : "")
                ).trim();

        return new SellerOrderResponse(
                order.getId(),
                orderItem.getId(),
                product.getId(),
                product.getName(),
                buyer.getId(),
                buyerName,
                buyer.getEmail(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getSubtotal(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt()
        );
    }

    // ==========================================
    // SELLER ANALYTICS
    // ==========================================

    public SellerAnalyticsResponse getSellerAnalytics(
            String sellerEmail
    ) {

        User seller = getSeller(sellerEmail);

        List<Product> products =
                productRepository.findBySeller(seller);

        List<OrderItem> orderItems =
                orderItemRepository.findByProductSellerId(
                        seller.getId()
                );

        long totalProducts = products.size();

        long totalOrders = orderItems.stream()
                .map(item ->
                        item.getOrder().getId()
                )
                .distinct()
                .count();

        long totalItemsSold = orderItems.stream()
                .filter(item ->
                        !"CANCELLED".equalsIgnoreCase(
                                item.getOrder().getStatus()
                        )
                )
                .mapToLong(OrderItem::getQuantity)
                .sum();

        BigDecimal totalRevenue = orderItems.stream()
                .filter(item ->
                        !"CANCELLED".equalsIgnoreCase(
                                item.getOrder().getStatus()
                        )
                )
                .map(OrderItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        long pendingOrders = orderItems.stream()
                .filter(item -> {

                    String status =
                            item.getOrder().getStatus();

                    return "PLACED".equalsIgnoreCase(status)
                            || "CONFIRMED".equalsIgnoreCase(status)
                            || "PROCESSING".equalsIgnoreCase(status)
                            || "SHIPPED".equalsIgnoreCase(status);
                })
                .map(item ->
                        item.getOrder().getId()
                )
                .distinct()
                .count();

        long completedOrders = orderItems.stream()
                .filter(item ->
                        "DELIVERED".equalsIgnoreCase(
                                item.getOrder().getStatus()
                        )
                )
                .map(item ->
                        item.getOrder().getId()
                )
                .distinct()
                .count();

        long cancelledOrders = orderItems.stream()
                .filter(item ->
                        "CANCELLED".equalsIgnoreCase(
                                item.getOrder().getStatus()
                        )
                )
                .map(item ->
                        item.getOrder().getId()
                )
                .distinct()
                .count();

        return new SellerAnalyticsResponse(
                totalProducts,
                totalOrders,
                totalItemsSold,
                totalRevenue,
                pendingOrders,
                completedOrders,
                cancelledOrders
        );
    }

    // ==========================================
    // ORDER RESPONSE
    // ==========================================

    private OrderResponse toResponse(Order order) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        return toResponse(order, orderItems);
    }

    private OrderResponse toResponse(
            Order order,
            List<OrderItem> orderItems
    ) {

        List<OrderItemResponse> itemResponses =
                orderItems.stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getProduct().getImageUrl(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getSubtotal()
                                )
                        )
                        .toList();

        Long shippingAddressId =
                order.getShippingAddress() != null
                        ? order.getShippingAddress().getId()
                        : null;

        return new OrderResponse(
                order.getId(),
                shippingAddressId,
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    // ==========================================
    // ADMIN DASHBOARD
    // ==========================================

    public AdminDashboardResponse getAdminDashboard() {

        long totalUsers = userRepository.count();
        long totalBuyers = userRepository.countByRoleIgnoreCase("BUYER");
        long totalSellers = userRepository.countByRoleIgnoreCase("SELLER");
        long totalProducts = productRepository.count();

        List<Order> orders = orderRepository.findAll();
        long totalOrders = orders.size();

        long pendingOrders = orders.stream()
                .filter(order -> {
                    String status = order.getStatus();
                    return "PLACED".equalsIgnoreCase(status)
                            || "CONFIRMED".equalsIgnoreCase(status)
                            || "PROCESSING".equalsIgnoreCase(status)
                            || "SHIPPED".equalsIgnoreCase(status);
                })
                .count();

        long deliveredOrders = orders.stream()
                .filter(order ->
                        "DELIVERED".equalsIgnoreCase(order.getStatus())
                )
                .count();

        long cancelledOrders = orders.stream()
                .filter(order ->
                        "CANCELLED".equalsIgnoreCase(order.getStatus())
                )
                .count();

        BigDecimal totalRevenue = orders.stream()
                .filter(order ->
                        !"CANCELLED".equalsIgnoreCase(order.getStatus())
                )
                .map(Order::getTotalAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new AdminDashboardResponse(
                totalUsers,
                totalBuyers,
                totalSellers,
                totalProducts,
                totalOrders,
                pendingOrders,
                deliveredOrders,
                cancelledOrders,
                totalRevenue
        );
    }

    // ==========================================
    // ADMIN ORDERS
    // ==========================================

    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .sorted((order1, order2) ->
                        order2.getCreatedAt()
                                .compareTo(
                                        order1.getCreatedAt()
                                )
                )
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getAdminOrderById(
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        return toResponse(order);
    }

    public OrderResponse updateOrderStatus(
            Long orderId,
            String status
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if (status == null || status.isBlank()) {
            throw new RuntimeException(
                    "Order status is required"
            );
        }

        String newStatus = status.toUpperCase();

        if (!newStatus.equals("PLACED") &&
                !newStatus.equals("CONFIRMED") &&
                !newStatus.equals("PROCESSING") &&
                !newStatus.equals("SHIPPED") &&
                !newStatus.equals("DELIVERED") &&
                !newStatus.equals("CANCELLED")) {

            throw new RuntimeException(
                    "Invalid order status"
            );
        }

        if ("CANCELLED".equalsIgnoreCase(
                order.getStatus()
        )) {

            throw new RuntimeException(
                    "Cancelled order status cannot be changed"
            );
        }

        if ("DELIVERED".equalsIgnoreCase(
                order.getStatus()
        )) {

            throw new RuntimeException(
                    "Delivered order status cannot be changed"
            );
        }

        order.setStatus(newStatus);

        Order savedOrder =
                orderRepository.save(order);

        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(savedOrder);
        tracking.setStatus(newStatus);
        tracking.setDescription("Order status updated to " + newStatus);
        orderTrackingRepository.save(tracking);

        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrderByAdmin(
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if ("CANCELLED".equalsIgnoreCase(
                order.getStatus()
        )) {

            throw new RuntimeException(
                    "Order is already cancelled"
            );
        }

        if ("DELIVERED".equalsIgnoreCase(
                order.getStatus()
        )) {

            throw new RuntimeException(
                    "Delivered order cannot be cancelled"
            );
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        for (OrderItem orderItem : orderItems) {

            Product product = orderItem.getProduct();

            product.setStock(
                    product.getStock()
                            + orderItem.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus("CANCELLED");

        Order savedOrder =
                orderRepository.save(order);

        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(savedOrder);
        tracking.setStatus("CANCELLED");
        tracking.setDescription("Order cancelled by admin");
        orderTrackingRepository.save(tracking);

        return toResponse(savedOrder);
    }

    public List<OrderResponse> getOrdersByStatus(
            String status
    ) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException(
                    "Order status is required"
            );
        }

        String orderStatus = status.toUpperCase();

        if (!orderStatus.equals("PLACED") &&
                !orderStatus.equals("CONFIRMED") &&
                !orderStatus.equals("PROCESSING") &&
                !orderStatus.equals("SHIPPED") &&
                !orderStatus.equals("DELIVERED") &&
                !orderStatus.equals("CANCELLED")) {

            throw new RuntimeException(
                    "Invalid order status"
            );
        }

        return orderRepository.findByStatus(orderStatus)
                .stream()
                .sorted((order1, order2) ->
                        order2.getCreatedAt()
                                .compareTo(
                                        order1.getCreatedAt()
                                )
                )
                .map(this::toResponse)
                .toList();
    }
}