package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.dao.*;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.OrderItem;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.InventoryService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Concrete Seller Dashboard extending BaseDashboard.
 * Demonstrates:
 * 1. OOP Inheritance: Extends BaseDashboard.
 * 2. Polymorphism: Implements DashboardView for the "SELLER" role.
 * 3. Metric analytics cards and seller inventory control.
 */
public class SellerDashboard extends BaseDashboard {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    private JPanel overviewPanel;
    private ProductManagementPanel productManagementPanel;
    private InventoryPanel inventoryPanel;
    private OrderPanel orderPanel;

    private JLabel totalProductsVal;
    private JLabel totalStockVal;
    private JLabel totalOrdersVal;
    private JLabel totalRevenueVal;

    public SellerDashboard(MainWindow parentWindow, User currentUser) {
        super(parentWindow, currentUser);

        var productDao = new JdbcProductDao();
        var categoryDao = new JdbcCategoryDao();
        var cartDao = new JdbcCartDao();
        var orderDao = new JdbcOrderDao();
        var inventoryDao = new JdbcInventoryDao();
        var addressDao = new JdbcAddressDao();
        var userDao = new JdbcUserDao();

        this.productService = new ProductService(productDao, categoryDao, null);
        this.inventoryService = new InventoryService(inventoryDao, productDao);
        this.orderService = new OrderService(orderDao, cartDao, productDao, addressDao, inventoryDao, userDao, null);

        initView();
    }

    @Override
    public void initView() {
        overviewPanel = createOverviewPanel();
        productManagementPanel = new ProductManagementPanel(productService, currentUser, false);
        inventoryPanel = new InventoryPanel(inventoryService, productService, currentUser);
        orderPanel = new OrderPanel(orderService, currentUser, true, false);

        mainContentContainer.add(overviewPanel, "OVERVIEW");
        mainContentContainer.add(productManagementPanel, "PRODUCTS");
        mainContentContainer.add(inventoryPanel, "INVENTORY");
        mainContentContainer.add(orderPanel, "ORDERS");

        selectNavCard("OVERVIEW");
        loadOverviewMetrics();
    }

    @Override
    protected void buildNavigation(JPanel navPanel) {
        addNavButton(navPanel, "OVERVIEW", "📊  Seller Overview");
        addNavButton(navPanel, "PRODUCTS", "📦  My Products");
        addNavButton(navPanel, "INVENTORY", "📋  Inventory & Stock");
        addNavButton(navPanel, "ORDERS", "🚚  Orders & Fulfillment");
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.BG_LIGHT);

        // Header Title
        JLabel title = new JLabel("Seller Performance Analytics & Store Overview");
        title.setFont(UITheme.FONT_SUBTITLE);
        panel.add(title, BorderLayout.NORTH);

        // 4 KPI Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsGrid.setOpaque(false);

        totalProductsVal = new JLabel("0");
        totalStockVal = new JLabel("0");
        totalOrdersVal = new JLabel("0");
        totalRevenueVal = new JLabel("₹0.00");

        metricsGrid.add(createMetricCard("Total Listed Products", totalProductsVal, UITheme.INDIGO_ACCENT));
        metricsGrid.add(createMetricCard("Total Stock Units", totalStockVal, UITheme.BLUE_ACCENT));
        metricsGrid.add(createMetricCard("Customer Orders", totalOrdersVal, UITheme.WARNING_AMBER));
        metricsGrid.add(createMetricCard("Estimated Revenue", totalRevenueVal, UITheme.SUCCESS_GREEN));

        panel.add(metricsGrid, BorderLayout.CENTER);

        // Quick action helper panel
        JPanel quickActions = UITheme.createCard();
        quickActions.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 0));
        JLabel actionTitle = new JLabel("Quick Store Actions: ");
        actionTitle.setFont(UITheme.FONT_BODY_BOLD);
        quickActions.add(actionTitle);

        JButton gotoProducts = UITheme.createPrimaryButton("+ Add New Product");
        gotoProducts.addActionListener(e -> selectNavCard("PRODUCTS"));
        quickActions.add(gotoProducts);

        JButton gotoInventory = UITheme.createSecondaryButton("Check Low Stock");
        gotoInventory.addActionListener(e -> selectNavCard("INVENTORY"));
        quickActions.add(gotoInventory);

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

    public void loadOverviewMetrics() {
        AsyncOperation.run(
                () -> {
                    List<Product> myProducts = productService.getProductsBySeller(currentUser.getId());
                    List<Order> myOrders = orderService.getOrdersForSeller(currentUser.getId());

                    int totalStock = 0;
                    for (Product p : myProducts) {
                        totalStock += (p.getStock() != null ? p.getStock() : 0);
                    }

                    BigDecimal revenue = BigDecimal.ZERO;
                    for (Order o : myOrders) {
                        List<OrderItem> items = orderService.getOrderItemsForSeller(o.getId(), currentUser.getId());
                        for (OrderItem oi : items) {
                            revenue = revenue.add(oi.getSubtotal());
                        }
                    }

                    return new Object[]{myProducts.size(), totalStock, myOrders.size(), revenue};
                },
                results -> {
                    totalProductsVal.setText(String.valueOf(results[0]));
                    totalStockVal.setText(String.valueOf(results[1]));
                    totalOrdersVal.setText(String.valueOf(results[2]));
                    totalRevenueVal.setText("₹" + String.format("%.2f", (BigDecimal) results[3]));
                },
                ex -> System.err.println("Failed to load seller metrics: " + ex.getMessage())
        );
    }

    @Override
    public void refreshData() {
        updateStatus("Refreshing seller data...");
        loadOverviewMetrics();
        if (productManagementPanel != null) productManagementPanel.loadProducts();
        if (inventoryPanel != null) inventoryPanel.loadInventory();
        if (orderPanel != null) orderPanel.loadOrders();
        updateStatus("Seller store synchronized");
    }

    @Override
    public String getRoleName() {
        return "SELLER";
    }
}
