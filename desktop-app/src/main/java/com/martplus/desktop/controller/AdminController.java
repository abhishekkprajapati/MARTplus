package com.martplus.desktop.controller;

import com.martplus.desktop.model.ActivityLog;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.ActivityService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.UserService;

import java.util.List;

/**
 * Controller mediating Administrative operations.
 */
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ActivityService activityService;

    public AdminController(UserService userService, ProductService productService,
                           OrderService orderService, ActivityService activityService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.activityService = activityService;
    }

    public List<User> listUsers(User admin, String roleFilter) {
        return userService.getUsersByRole(admin, roleFilter);
    }

    public void updateUserStatus(User admin, Long userId, String status) {
        userService.updateUserStatus(admin, userId, status);
    }

    public List<Product> listProducts(User admin) {
        return productService.getAllProductsForAdmin(admin);
    }

    public List<Order> listOrders(User admin, String status) {
        return orderService.getAllOrdersForAdmin(admin, status);
    }

    public List<ActivityLog> listLogs(int limit) {
        return activityService.getRecentLogs(limit);
    }
}
