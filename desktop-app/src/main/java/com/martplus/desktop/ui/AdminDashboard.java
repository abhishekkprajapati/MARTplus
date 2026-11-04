package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.dao.*;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.ActivityService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.UserService;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Concrete Admin Dashboard extending BaseDashboard.
 * Demonstrates:
 * 1. OOP Inheritance: Extends BaseDashboard.
 * 2. Polymorphism: Implements DashboardView for the "ADMIN" role.
 * 3. Platform-wide user administration, catalog oversight, and audit trails.
 */
public class AdminDashboard extends BaseDashboard {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ActivityService activityService;

    private JPanel overviewPanel;
    private UserManagementPanel userManagementPanel;
    private ProductManagementPanel productManagementPanel;
    private OrderPanel orderPanel;
    private ActivityLogPanel activityLogPanel;

    private JLabel totalUsersVal;
    private JLabel totalProductsVal;
    private JLabel totalOrdersVal;
    private JLabel platformVolumeVal;

    public AdminDashboard(MainWindow parentWindow, User currentUser) {
        super(parentWindow, currentUser);

        var userDao = new JdbcUserDao();
        var productDao = new JdbcProductDao();
        var categoryDao = new JdbcCategoryDao();
        var cartDao = new JdbcCartDao();
        var orderDao = new JdbcOrderDao();
        var inventoryDao = new JdbcInventoryDao();
        var addressDao = new JdbcAddressDao();
        var activityLogDao = new JdbcActivityLogDao();

        this.activityService = new ActivityService(activityLogDao);
        this.userService = new UserService(userDao, activityService);
        this.productService = new ProductService(productDao, categoryDao, activityService);
        this.orderService = new OrderService(orderDao, cartDao, productDao, addressDao, inventoryDao, userDao, activityService);

        initView();
    }

    @Override
    public void initView() {
        overviewPanel = createOverviewPanel();
        userManagementPanel = new UserManagementPanel(userService, currentUser);
        productManagementPanel = new ProductManagementPanel(productService, currentUser, true);
        orderPanel = new OrderPanel(orderService, currentUser, false, true);
        activityLogPanel = new ActivityLogPanel(activityService);

        mainContentContainer.add(overviewPanel, "OVERVIEW");
        mainContentContainer.add(userManagementPanel, "USERS");
        mainContentContainer.add(productManagementPanel, "CATALOG");
        mainContentContainer.add(orderPanel, "ORDERS");
        mainContentContainer.add(activityLogPanel, "LOGS");

        selectNavCard("OVERVIEW");
        loadAdminMetrics();
    }

    @Override
    protected void buildNavigation(JPanel navPanel) {
        addNavButton(navPanel, "OVERVIEW", "📊  Platform Overview");
        addNavButton(navPanel, "USERS", "👥  User Management");
        addNavButton(navPanel, "CATALOG", "📦  Catalog Oversight");
        addNavButton(navPanel, "ORDERS", "📑  Order Management");
        addNavButton(navPanel, "LOGS", "🛡️  Activity Logs");
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.BG_LIGHT);

        JLabel title = new JLabel("MARTplus Platform Administration & Governance");
        title.setFont(UITheme.FONT_SUBTITLE);
        panel.add(title, BorderLayout.NORTH);

        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsGrid.setOpaque(false);

        totalUsersVal = new JLabel("0");
        totalProductsVal = new JLabel("0");
        totalOrdersVal = new JLabel("0");
        platformVolumeVal = new JLabel("₹0.00");

        metricsGrid.add(createMetricCard("Registered Users", totalUsersVal, UITheme.INDIGO_ACCENT));
        metricsGrid.add(createMetricCard("Total Listed Items", totalProductsVal, UITheme.BLUE_ACCENT));
        metricsGrid.add(createMetricCard("Platform Orders", totalOrdersVal, UITheme.WARNING_AMBER));
        metricsGrid.add(createMetricCard("Total GMV Volume", platformVolumeVal, UITheme.SUCCESS_GREEN));

        panel.add(metricsGrid, BorderLayout.CENTER);

        JPanel quickActions = UITheme.createCard();
        quickActions.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 0));
        JLabel actionTitle = new JLabel("Quick Administrative Actions: ");
        actionTitle.setFont(UITheme.FONT_BODY_BOLD);
        quickActions.add(actionTitle);

        JButton gotoUsers = UITheme.createPrimaryButton("+ Add System User");
        gotoUsers.addActionListener(e -> selectNavCard("USERS"));
        quickActions.add(gotoUsers);

        JButton gotoOrders = UITheme.createSecondaryButton("View Pending Orders");
        gotoOrders.addActionListener(e -> selectNavCard("ORDERS"));
        quickActions.add(gotoOrders);

        JButton gotoLogs = UITheme.createSecondaryButton("View Audit Trail");
        gotoLogs.addActionListener(e -> selectNavCard("LOGS"));
        quickActions.add(gotoLogs);

        panel.add(quickActions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(0, 8));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BODY);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadAdminMetrics() {
        AsyncOperation.run(
                () -> {
                    List<User> users = userService.getAllUsers(currentUser);
                    List<Product> products = productService.getAllProductsForAdmin(currentUser);
                    List<Order> orders = orderService.getAllOrdersForAdmin(currentUser, "ALL");

                    BigDecimal totalGMV = BigDecimal.ZERO;
                    for (Order o : orders) {
                        if (o.getTotalAmount() != null) {
                            totalGMV = totalGMV.add(o.getTotalAmount());
                        }
                    }

                    return new Object[]{users.size(), products.size(), orders.size(), totalGMV};
                },
                results -> {
                    totalUsersVal.setText(String.valueOf(results[0]));
                    totalProductsVal.setText(String.valueOf(results[1]));
                    totalOrdersVal.setText(String.valueOf(results[2]));
                    platformVolumeVal.setText("₹" + String.format("%.2f", (BigDecimal) results[3]));
                },
                ex -> System.err.println("Failed to load admin metrics: " + ex.getMessage())
        );
    }

    @Override
    public void refreshData() {
        updateStatus("Refreshing platform administration metrics...");
        loadAdminMetrics();
        if (userManagementPanel != null) userManagementPanel.loadUsers();
        if (productManagementPanel != null) productManagementPanel.loadProducts();
        if (orderPanel != null) orderPanel.loadOrders();
        if (activityLogPanel != null) activityLogPanel.loadLogs();
        updateStatus("Administration data synchronized");
    }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }
}
