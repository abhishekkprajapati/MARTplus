package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.dao.AddressDao;
import com.martplus.desktop.dao.JdbcAddressDao;
import com.martplus.desktop.dao.JdbcCartDao;
import com.martplus.desktop.dao.JdbcCategoryDao;
import com.martplus.desktop.dao.JdbcInventoryDao;
import com.martplus.desktop.dao.JdbcOrderDao;
import com.martplus.desktop.dao.JdbcProductDao;
import com.martplus.desktop.dao.JdbcUserDao;
import com.martplus.desktop.model.Address;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.ActivityService;
import com.martplus.desktop.service.CartService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.UserService;
import com.martplus.desktop.service.WishlistService;
import com.martplus.desktop.dao.JdbcWishlistDao;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Concrete Buyer Dashboard extending BaseDashboard.
 * Demonstrates:
 * 1. OOP Inheritance: Extends BaseDashboard and leverages unified lifecycle and layout.
 * 2. Polymorphism: Implements DashboardView for the "BUYER" role.
 */
public class BuyerDashboard extends BaseDashboard {

    private ProductPanel productPanel;
    private CartPanel cartPanel;
    private WishlistPanel wishlistPanel;
    private OrderPanel orderPanel;
    private JPanel addressPanel;
    private JPanel profilePanel;

    private final ProductService productService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final OrderService orderService;
    private final UserService userService;
    private final AddressDao addressDao;

    public BuyerDashboard(MainWindow parentWindow, User currentUser) {
        super(parentWindow, currentUser);

        // Wire service dependencies using JDBC DAOs
        var productDao = new JdbcProductDao();
        var categoryDao = new JdbcCategoryDao();
        var cartDao = new JdbcCartDao();
        var wishlistDao = new JdbcWishlistDao();
        var orderDao = new JdbcOrderDao();
        var userDao = new JdbcUserDao();
        var inventoryDao = new JdbcInventoryDao();
        this.addressDao = new JdbcAddressDao();

        this.productService = new ProductService(productDao, categoryDao, null);
        this.cartService = new CartService(cartDao, productDao);
        this.wishlistService = new WishlistService(wishlistDao);
        this.orderService = new OrderService(orderDao, cartDao, productDao, addressDao, inventoryDao, userDao, null);
        this.userService = new UserService(userDao, null);

        initView();
    }

    @Override
    public void initView() {
        productPanel = new ProductPanel(productService, cartService, wishlistService, currentUser);
        cartPanel = new CartPanel(cartService, orderService, addressDao, currentUser, () -> {
            if (orderPanel != null) {
                orderPanel.loadOrders();
            }
        });
        wishlistPanel = new WishlistPanel(wishlistService, cartService, currentUser);
        orderPanel = new OrderPanel(orderService, currentUser, false, false);
        addressPanel = createAddressManagementPanel();
        profilePanel = createProfilePanel();

        mainContentContainer.add(productPanel, "PRODUCTS");
        mainContentContainer.add(cartPanel, "CART");
        mainContentContainer.add(wishlistPanel, "WISHLIST");
        mainContentContainer.add(orderPanel, "ORDERS");
        mainContentContainer.add(addressPanel, "ADDRESSES");
        mainContentContainer.add(profilePanel, "PROFILE");

        selectNavCard("PRODUCTS");
    }

    @Override
    protected void buildNavigation(JPanel navPanel) {
        addNavButton(navPanel, "PRODUCTS", "🛍️  Browse Products");
        addNavButton(navPanel, "CART", "🛒  Shopping Cart");
        addNavButton(navPanel, "WISHLIST", "♡  My Wishlist");
        addNavButton(navPanel, "ORDERS", "📦  My Orders");
        addNavButton(navPanel, "ADDRESSES", "📍  Saved Addresses");
        addNavButton(navPanel, "PROFILE", "👤  My Profile");
    }

    @Override
    public void refreshData() {
        updateStatus("Refreshing buyer data from PostgreSQL...");
        if (productPanel != null) productPanel.loadProducts();
        if (cartPanel != null) cartPanel.loadCart();
        if (wishlistPanel != null) wishlistPanel.loadWishlist();
        if (orderPanel != null) orderPanel.loadOrders();
        updateStatus("Buyer data synchronized");
    }

    @Override
    public String getRoleName() {
        return "BUYER";
    }

    private JPanel createAddressManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);

        JPanel headerCard = UITheme.createCard();
        headerCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        JLabel titleLbl = new JLabel("Your Delivery Addresses");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        headerCard.add(titleLbl);
        panel.add(headerCard, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Phone", "Address", "City", "State", "PIN Code", "Default"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        JPanel actionCard = UITheme.createCard();
        actionCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton addBtn = UITheme.createPrimaryButton("+ Add New Address");
        addBtn.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField phone = new JTextField();
            JTextField line = new JTextField();
            JTextField city = new JTextField();
            JTextField state = new JTextField();
            JTextField pin = new JTextField();
            JCheckBox def = new JCheckBox("Set as default delivery address");

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Full Name:")); form.add(name);
            form.add(new JLabel("Phone (10 digits):")); form.add(phone);
            form.add(new JLabel("Address Line:")); form.add(line);
            form.add(new JLabel("City:")); form.add(city);
            form.add(new JLabel("State:")); form.add(state);
            form.add(new JLabel("PIN Code:")); form.add(pin);
            form.add(new JLabel("Default:")); form.add(def);

            int opt = JOptionPane.showConfirmDialog(this, form, "Add Address", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                Address a = new Address(null, currentUser.getId(), name.getText(), phone.getText(),
                        line.getText(), city.getText(), state.getText(), pin.getText(), def.isSelected(), null, null);
                addressDao.save(a);
                refreshAddressTable(model);
            }
        });

        JButton deleteBtn = UITheme.createDangerButton("Delete Address");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long addrId = (Long) model.getValueAt(row, 0);
                if (DialogUtil.confirm(this, "Delete selected address?", "Confirm Delete")) {
                    addressDao.deleteById(addrId);
                    refreshAddressTable(model);
                }
            } else {
                DialogUtil.showWarning(this, "Select an address from the table.");
            }
        });

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAddressTable(model));

        actionCard.add(addBtn);
        actionCard.add(deleteBtn);
        actionCard.add(refreshBtn);
        panel.add(actionCard, BorderLayout.SOUTH);

        refreshAddressTable(model);
        return panel;
    }

    private void refreshAddressTable(DefaultTableModel model) {
        AsyncOperation.run(
                () -> addressDao.findByBuyerId(currentUser.getId()),
                list -> {
                    model.setRowCount(0);
                    for (Address a : list) {
                        model.addRow(new Object[]{
                                a.getId(), a.getFullName(), a.getPhone(),
                                a.getAddressLine(), a.getCity(), a.getState(),
                                a.getPincode(), a.isDefaultAddress() ? "Default" : "No"
                        });
                    }
                },
                ex -> System.err.println("Failed to load addresses: " + ex.getMessage())
        );
    }

    private JPanel createProfilePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Account Profile Details");
        title.setFont(UITheme.FONT_SUBTITLE);
        card.add(title, BorderLayout.NORTH);

        JTextField firstField = new JTextField(currentUser.getFirstName());
        JTextField lastField = new JTextField(currentUser.getLastName());
        JTextField emailField = new JTextField(currentUser.getEmail());
        emailField.setEditable(false); // Protected primary identifier
        JTextField phoneField = new JTextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        JTextField roleField = new JTextField(currentUser.getRole());
        roleField.setEditable(false);
        JTextField statusField = new JTextField(currentUser.getStatus());
        statusField.setEditable(false);

        form.add(new JLabel("First Name:")); form.add(firstField);
        form.add(new JLabel("Last Name:")); form.add(lastField);
        form.add(new JLabel("Email Address (Immutable):")); form.add(emailField);
        form.add(new JLabel("Phone Number:")); form.add(phoneField);
        form.add(new JLabel("Role:")); form.add(roleField);
        form.add(new JLabel("Account Status:")); form.add(statusField);

        JButton saveBtn = UITheme.createPrimaryButton("Save Profile Changes");
        saveBtn.addActionListener(e -> {
            AsyncOperation.run(
                    () -> {
                        userService.updateProfile(currentUser, firstField.getText(), lastField.getText(), phoneField.getText());
                        return null;
                    },
                    res -> DialogUtil.showSuccess(this, "Profile updated successfully!"),
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(saveBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }
}
