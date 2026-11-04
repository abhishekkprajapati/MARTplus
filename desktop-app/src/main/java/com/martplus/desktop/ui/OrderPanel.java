package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.OrderItem;
import com.martplus.desktop.model.OrderTracking;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.util.DateUtil;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing Orders, order items inspection, tracking timeline, and status updates.
 */
public class OrderPanel extends JPanel {

    private final OrderService orderService;
    private final User currentUser;
    private final boolean isSellerView;
    private final boolean isAdminView;

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;
    private List<Order> currentOrders = new ArrayList<>();

    public OrderPanel(OrderService orderService, User currentUser, boolean isSellerView, boolean isAdminView) {
        this.orderService = orderService;
        this.currentUser = currentUser;
        this.isSellerView = isSellerView;
        this.isAdminView = isAdminView;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTopFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        loadOrders();
    }

    private JPanel createTopFilterPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        String title = isAdminView ? "All Customer Orders" :
                isSellerView ? "Orders Containing Your Products" : "Your Order History";
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        card.add(titleLbl);

        card.add(Box.createRigidArea(new Dimension(20, 0)));
        card.add(new JLabel("Filter Status:"));

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PLACED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"});
        statusFilterCombo.setFont(UITheme.FONT_BODY);
        statusFilterCombo.addActionListener(e -> loadOrders());
        card.add(statusFilterCombo);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadOrders());
        card.add(refreshBtn);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        String[] cols = {"Order #", "Buyer", "Date", "Total Amount (₹)", "Payment Method", "Payment Status", "Order Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel);
        UITheme.styleTable(ordersTable);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton viewItemsBtn = UITheme.createSecondaryButton("View Order Items");
        viewItemsBtn.addActionListener(e -> handleViewItems());
        card.add(viewItemsBtn);

        JButton trackOrderBtn = UITheme.createSecondaryButton("🚚 Track Timeline");
        trackOrderBtn.addActionListener(e -> handleTrackOrder());
        card.add(trackOrderBtn);

        if (isAdminView || isSellerView) {
            JButton updateStatusBtn = UITheme.createPrimaryButton("Update Order Status");
            updateStatusBtn.addActionListener(e -> handleUpdateStatus());
            card.add(updateStatusBtn);
        }

        return card;
    }

    public void loadOrders() {
        String filter = (String) statusFilterCombo.getSelectedItem();

        AsyncOperation.run(
                () -> {
                    if (isAdminView) {
                        return orderService.getAllOrdersForAdmin(currentUser, filter);
                    } else if (isSellerView) {
                        return orderService.getOrdersForSeller(currentUser.getId());
                    } else {
                        return orderService.getOrdersForBuyer(currentUser.getId());
                    }
                },
                list -> {
                    currentOrders = list;
                    tableModel.setRowCount(0);
                    for (Order o : list) {
                        // In seller/buyer views, apply in-memory status filter if selected
                        if (filter != null && !filter.equalsIgnoreCase("ALL") &&
                                !o.getStatus().equalsIgnoreCase(filter)) {
                            continue;
                        }

                        tableModel.addRow(new Object[]{
                                o.getId(),
                                o.getBuyerName() != null ? o.getBuyerName() : "Buyer #" + o.getBuyerId(),
                                DateUtil.formatDateTime(o.getCreatedAt()),
                                String.format("%.2f", o.getTotalAmount()),
                                o.getPaymentMethod(),
                                o.getPaymentStatus(),
                                o.getStatus()
                        });
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load orders: " + ex.getMessage())
        );
    }

    private void handleViewItems() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an order from the table.");
            return;
        }

        Order order = currentOrders.get(row);
        AsyncOperation.run(
                () -> isSellerView
                        ? orderService.getOrderItemsForSeller(order.getId(), currentUser.getId())
                        : orderService.getOrderItems(order.getId()),
                items -> showItemsDialog(order, items),
                ex -> DialogUtil.showError(this, "Failed to load order items: " + ex.getMessage())
        );
    }

    private void showItemsDialog(Order order, List<OrderItem> items) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Order #" + order.getId() + " - Items Details", true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(560, 340);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Item ID", "Product Name", "Category", "Quantity", "Unit Price (₹)", "Subtotal (₹)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (OrderItem oi : items) {
            model.addRow(new Object[]{
                    oi.getId(),
                    oi.getProductName() != null ? oi.getProductName() : "Product #" + oi.getProductId(),
                    oi.getProductCategory() != null ? oi.getProductCategory() : "-",
                    oi.getQuantity(),
                    String.format("%.2f", oi.getPrice()),
                    String.format("%.2f", oi.getSubtotal())
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton close = UITheme.createPrimaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        closePanel.add(close);
        dialog.add(closePanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleTrackOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an order from the table.");
            return;
        }

        Order order = currentOrders.get(row);
        AsyncOperation.run(
                () -> orderService.getOrderTracking(order.getId()),
                trackingList -> showTrackingDialog(order, trackingList),
                ex -> DialogUtil.showError(this, "Failed to load tracking: " + ex.getMessage())
        );
    }

    private void showTrackingDialog(Order order, List<OrderTracking> list) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tracking Timeline - Order #" + order.getId(), true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(500, 360);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(Color.WHITE);

        if (list.isEmpty()) {
            content.add(new JLabel("No tracking milestones recorded yet for this order."));
        } else {
            for (OrderTracking ot : list) {
                JPanel item = new JPanel(new BorderLayout());
                item.setBackground(Color.WHITE);
                item.setBorder(new EmptyBorder(6, 0, 10, 0));

                JLabel status = new JLabel("● " + ot.getStatus());
                status.setFont(UITheme.FONT_BODY_BOLD);
                status.setForeground(UITheme.INDIGO_ACCENT);

                JLabel desc = new JLabel(ot.getDescription());
                desc.setFont(UITheme.FONT_BODY);

                JLabel time = new JLabel(DateUtil.formatDateTime(ot.getCreatedAt()));
                time.setFont(UITheme.FONT_SMALL);
                time.setForeground(UITheme.TEXT_MUTED);

                item.add(status, BorderLayout.NORTH);
                item.add(desc, BorderLayout.CENTER);
                item.add(time, BorderLayout.SOUTH);

                content.add(item);
                content.add(new JSeparator(JSeparator.HORIZONTAL));
            }
        }

        dialog.add(new JScrollPane(content), BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton close = UITheme.createPrimaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        closePanel.add(close);
        dialog.add(closePanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleUpdateStatus() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an order from the table.");
            return;
        }

        Order order = currentOrders.get(row);
        String[] statuses = {"CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new status for Order #" + order.getId() + " (Current: " + order.getStatus() + "):",
                "Update Order Status", JOptionPane.QUESTION_MESSAGE, null, statuses, order.getStatus());

        if (selected == null || selected.isBlank()) return;

        AsyncOperation.run(
                () -> {
                    orderService.updateOrderStatus(currentUser, order.getId(), selected);
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Order #" + order.getId() + " status updated to " + selected);
                    loadOrders();
                },
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }
}
