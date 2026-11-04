package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.InventoryService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Seller stock management, low-stock alerts, and synchronized updates.
 * Demonstrates:
 * 1. Synchronized inventory updates via ReentrantLock.
 * 2. Real-time stock status monitoring.
 */
public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final User currentUser;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JCheckBox lowStockOnlyCheck;
    private JLabel lowStockAlertBanner;
    private List<Product> products = new ArrayList<>();

    public InventoryPanel(InventoryService inventoryService, ProductService productService, User currentUser) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTopFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        loadInventory();
    }

    private JPanel createTopFilterPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setOpaque(false);

        // Low stock warning banner
        lowStockAlertBanner = new JLabel("  ⚠️ Monitoring stock levels. Products below 5 units will be highlighted.");
        lowStockAlertBanner.setFont(UITheme.FONT_BODY_BOLD);
        lowStockAlertBanner.setForeground(new Color(0xB4, 0x53, 0x09));
        lowStockAlertBanner.setBackground(new Color(0xFE, 0xF3, 0xC7));
        lowStockAlertBanner.setOpaque(true);
        lowStockAlertBanner.setBorder(new EmptyBorder(8, 12, 8, 12));
        container.add(lowStockAlertBanner, BorderLayout.NORTH);

        JPanel filterCard = UITheme.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 0));

        lowStockOnlyCheck = new JCheckBox("Show Low Stock Products Only (<= 5 units)");
        lowStockOnlyCheck.setFont(UITheme.FONT_BODY);
        lowStockOnlyCheck.setBackground(Color.WHITE);
        lowStockOnlyCheck.addActionListener(e -> loadInventory());
        filterCard.add(lowStockOnlyCheck);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Stock");
        refreshBtn.addActionListener(e -> loadInventory());
        filterCard.add(refreshBtn);

        container.add(filterCard, BorderLayout.CENTER);
        return container;
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        String[] cols = {"Product ID", "Product Name", "Category", "Price (₹)", "Current Stock", "Stock Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        UITheme.styleTable(inventoryTable);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton quickAdd5 = UITheme.createSecondaryButton("+5 Stock");
        quickAdd5.addActionListener(e -> handleQuickAdjust(5));
        card.add(quickAdd5);

        JButton quickAdd20 = UITheme.createSecondaryButton("+20 Stock");
        quickAdd20.addActionListener(e -> handleQuickAdjust(20));
        card.add(quickAdd20);

        JButton updateStockBtn = UITheme.createPrimaryButton("Set Exact Stock Quantity");
        updateStockBtn.addActionListener(e -> handleSetStock());
        card.add(updateStockBtn);

        return card;
    }

    public void loadInventory() {
        boolean lowOnly = lowStockOnlyCheck.isSelected();

        AsyncOperation.run(
                () -> lowOnly
                        ? inventoryService.getLowStockAlerts(currentUser.getId(), 5)
                        : productService.getProductsBySeller(currentUser.getId()),
                list -> {
                    products = list;
                    tableModel.setRowCount(0);
                    int lowStockCount = 0;

                    for (Product p : list) {
                        int stock = p.getStock() != null ? p.getStock() : 0;
                        String statusText;
                        if (stock == 0) {
                            statusText = "OUT OF STOCK";
                            lowStockCount++;
                        } else if (stock <= 5) {
                            statusText = "LOW STOCK (" + stock + " left)";
                            lowStockCount++;
                        } else {
                            statusText = "IN STOCK";
                        }

                        tableModel.addRow(new Object[]{
                                p.getId(),
                                p.getName(),
                                p.getCategory(),
                                String.format("%.2f", p.getPrice()),
                                stock,
                                statusText
                        });
                    }

                    if (lowStockCount > 0) {
                        lowStockAlertBanner.setText("  ⚠️ Low Stock Alert: " + lowStockCount + " product(s) have 5 or fewer items remaining in stock!");
                        lowStockAlertBanner.setVisible(true);
                    } else {
                        lowStockAlertBanner.setText("  ✓ All active products have healthy stock levels (> 5 units).");
                        lowStockAlertBanner.setVisible(true);
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load inventory: " + ex.getMessage())
        );
    }

    private void handleSetStock() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a product from the inventory table.");
            return;
        }

        Product p = products.get(row);
        String input = JOptionPane.showInputDialog(this,
                "Set new total stock for '" + p.getName() + "':", String.valueOf(p.getStock()));
        if (input == null || input.isBlank()) return;

        try {
            int newStock = Integer.parseInt(input.trim());
            if (newStock < 0) {
                DialogUtil.showWarning(this, "Stock cannot be negative.");
                return;
            }

            AsyncOperation.run(
                    () -> {
                        inventoryService.updateStock(p.getId(), newStock);
                        return null;
                    },
                    res -> {
                        DialogUtil.showSuccess(this, "Stock updated to " + newStock + " for '" + p.getName() + "'");
                        loadInventory();
                    },
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        } catch (NumberFormatException e) {
            DialogUtil.showWarning(this, "Please enter a valid whole number.");
        }
    }

    private void handleQuickAdjust(int delta) {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a product from the inventory table.");
            return;
        }

        Product p = products.get(row);
        AsyncOperation.run(
                () -> {
                    inventoryService.adjustStock(p.getId(), delta);
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Added +" + delta + " stock to '" + p.getName() + "'");
                    loadInventory();
                },
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }
}
