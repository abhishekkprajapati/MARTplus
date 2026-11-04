package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.dao.AddressDao;
import com.martplus.desktop.model.Address;
import com.martplus.desktop.model.CartItem;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.CartService;
import com.martplus.desktop.service.OrderService;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing Shopping Cart and initiating Transaction-safe Checkout.
 * Demonstrates:
 * 1. Collections: List<CartItem>.
 * 2. Atomic multi-step checkout execution via OrderService.
 * 3. Responsive UI with EDT safety.
 */
public class CartPanel extends JPanel {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressDao addressDao;
    private final User currentUser;
    private final Runnable onOrderPlacedCallback;

    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;
    private JButton checkoutBtn;
    private JButton updateQtyBtn;
    private JButton removeBtn;
    private JButton clearCartBtn;
    private List<CartItem> cartItems = new ArrayList<>();

    public CartPanel(CartService cartService, OrderService orderService,
                     AddressDao addressDao, User currentUser, Runnable onOrderPlacedCallback) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressDao = addressDao;
        this.currentUser = currentUser;
        this.onOrderPlacedCallback = onOrderPlacedCallback;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomSummaryPanel(), BorderLayout.SOUTH);

        loadCart();
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        JLabel header = new JLabel("Your Shopping Cart");
        header.setFont(UITheme.FONT_SUBTITLE);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"Item ID", "Product Name", "Unit Price (₹)", "Quantity", "Subtotal (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        cartTable = new JTable(tableModel);
        UITheme.styleTable(cartTable);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomSummaryPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        // Left controls (update quantity, remove, clear)
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setBackground(Color.WHITE);

        updateQtyBtn = UITheme.createSecondaryButton("Update Qty");
        updateQtyBtn.addActionListener(e -> handleUpdateQuantity());
        leftActions.add(updateQtyBtn);

        removeBtn = UITheme.createDangerButton("Remove Item");
        removeBtn.addActionListener(e -> handleRemoveItem());
        leftActions.add(removeBtn);

        clearCartBtn = UITheme.createSecondaryButton("Clear Cart");
        clearCartBtn.addActionListener(e -> handleClearCart());
        leftActions.add(clearCartBtn);

        // Right summary (Total & Checkout)
        JPanel rightSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightSummary.setBackground(Color.WHITE);

        totalAmountLabel = new JLabel("Total: ₹0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalAmountLabel.setForeground(UITheme.NAVY_PRIMARY);
        rightSummary.add(totalAmountLabel);

        checkoutBtn = UITheme.createPrimaryButton("Proceed to Checkout ➔");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> handleCheckout());
        rightSummary.add(checkoutBtn);

        card.add(leftActions, BorderLayout.WEST);
        card.add(rightSummary, BorderLayout.EAST);
        return card;
    }

    public void loadCart() {
        AsyncOperation.run(
                () -> cartService.getCartItems(currentUser.getId()),
                items -> {
                    cartItems = items;
                    tableModel.setRowCount(0);
                    BigDecimal total = BigDecimal.ZERO;

                    for (CartItem ci : items) {
                        String prodName = ci.getProduct() != null ? ci.getProduct().getName() : "Product #" + ci.getProductId();
                        BigDecimal price = ci.getProduct() != null ? ci.getProduct().getPrice() : BigDecimal.ZERO;
                        BigDecimal subtotal = ci.getSubtotal();
                        total = total.add(subtotal);

                        tableModel.addRow(new Object[]{
                                ci.getId(),
                                prodName,
                                String.format("%.2f", price),
                                ci.getQuantity(),
                                String.format("%.2f", subtotal)
                        });
                    }

                    totalAmountLabel.setText("Total: ₹" + String.format("%.2f", total));
                    boolean hasItems = !items.isEmpty();
                    checkoutBtn.setEnabled(hasItems);
                    updateQtyBtn.setEnabled(hasItems);
                    removeBtn.setEnabled(hasItems);
                    clearCartBtn.setEnabled(hasItems);
                },
                ex -> DialogUtil.showError(this, "Failed to load cart: " + ex.getMessage())
        );
    }

    private void handleUpdateQuantity() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an item in the cart to update.");
            return;
        }

        CartItem item = cartItems.get(row);
        String input = JOptionPane.showInputDialog(this,
                "Enter new quantity for item:", String.valueOf(item.getQuantity()));
        if (input == null || input.isBlank()) return;

        try {
            int newQty = Integer.parseInt(input.trim());
            if (newQty <= 0) {
                DialogUtil.showWarning(this, "Quantity must be at least 1 (or use Remove).");
                return;
            }

            AsyncOperation.run(
                    () -> {
                        cartService.updateQuantity(item.getId(), newQty);
                        return null;
                    },
                    res -> loadCart(),
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        } catch (NumberFormatException e) {
            DialogUtil.showWarning(this, "Invalid number entered.");
        }
    }

    private void handleRemoveItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an item in the cart to remove.");
            return;
        }

        CartItem item = cartItems.get(row);
        if (DialogUtil.confirm(this, "Remove item from cart?", "Confirm Removal")) {
            AsyncOperation.run(
                    () -> {
                        cartService.removeFromCart(item.getId());
                        return null;
                    },
                    res -> loadCart(),
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        }
    }

    private void handleClearCart() {
        if (DialogUtil.confirm(this, "Clear all items from your cart?", "Confirm Clear Cart")) {
            AsyncOperation.run(
                    () -> {
                        cartService.clearCart(currentUser.getId());
                        return null;
                    },
                    res -> loadCart(),
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            DialogUtil.showWarning(this, "Your cart is empty.");
            return;
        }

        // Fetch addresses for checkout dialog
        List<Address> addresses = addressDao.findByBuyerId(currentUser.getId());
        if (addresses.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "You do not have any delivery addresses saved. Would you like to add one now?",
                    "No Address Found", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                promptAddNewAddress();
            }
            return;
        }

        showCheckoutDialog(addresses);
    }

    private void showCheckoutDialog(List<Address> addresses) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirm Order & Checkout", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Select Shipping Address:"));
        JComboBox<Address> addressCombo = new JComboBox<>(addresses.toArray(new Address[0]));
        addressCombo.setFont(UITheme.FONT_BODY);
        panel.add(addressCombo);

        JButton addAddressBtn = UITheme.createSecondaryButton("+ Add New Address");
        addAddressBtn.addActionListener(e -> {
            promptAddNewAddress();
            dialog.dispose();
            handleCheckout();
        });
        panel.add(addAddressBtn);

        panel.add(new JLabel("Select Payment Method:"));
        JComboBox<String> paymentCombo = new JComboBox<>(new String[]{"COD", "UPI", "CREDIT_CARD", "DEBIT_CARD", "NET_BANKING"});
        paymentCombo.setFont(UITheme.FONT_BODY);
        panel.add(paymentCombo);

        BigDecimal total = cartService.calculateCartTotal(cartItems);
        JLabel summaryLabel = new JLabel("Total Payable: ₹" + String.format("%.2f", total));
        summaryLabel.setFont(UITheme.FONT_SUBTITLE);
        summaryLabel.setForeground(UITheme.INDIGO_ACCENT);
        panel.add(summaryLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton placeOrderBtn = UITheme.createPrimaryButton("Place Order Now");
        placeOrderBtn.addActionListener(e -> {
            Address selectedAddr = (Address) addressCombo.getSelectedItem();
            String paymentMethod = (String) paymentCombo.getSelectedItem();
            if (selectedAddr == null) {
                DialogUtil.showWarning(dialog, "Please select an address.");
                return;
            }

            placeOrderBtn.setEnabled(false);
            placeOrderBtn.setText("Processing Transaction...");

            AsyncOperation.run(
                    () -> orderService.processCheckout(currentUser, selectedAddr.getId(), paymentMethod),
                    order -> {
                        dialog.dispose();
                        DialogUtil.showSuccess(this,
                                "Order #" + order.getId() + " placed successfully!\n" +
                                "Total: ₹" + order.getTotalAmount() + "\n" +
                                "Payment: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n" +
                                "A tracking milestone has been created.");
                        loadCart();
                        if (onOrderPlacedCallback != null) {
                            onOrderPlacedCallback.run();
                        }
                    },
                    ex -> {
                        placeOrderBtn.setEnabled(true);
                        placeOrderBtn.setText("Place Order Now");
                        DialogUtil.showError(dialog, "Checkout Failed: " + ex.getMessage());
                    }
            );
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(placeOrderBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void promptAddNewAddress() {
        JTextField nameField = new JTextField(currentUser.getFullName());
        JTextField phoneField = new JTextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        JTextField lineField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField stateField = new JTextField();
        JTextField pinField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Phone (10 digits):"));
        form.add(phoneField);
        form.add(new JLabel("Street Address:"));
        form.add(lineField);
        form.add(new JLabel("City:"));
        form.add(cityField);
        form.add(new JLabel("State:"));
        form.add(stateField);
        form.add(new JLabel("PIN Code:"));
        form.add(pinField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Delivery Address",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Address address = new Address(
                        null,
                        currentUser.getId(),
                        nameField.getText().trim(),
                        phoneField.getText().trim(),
                        lineField.getText().trim(),
                        cityField.getText().trim(),
                        stateField.getText().trim(),
                        pinField.getText().trim(),
                        true,
                        null,
                        null
                );
                addressDao.save(address);
                DialogUtil.showSuccess(this, "Address saved successfully!");
            } catch (Exception ex) {
                DialogUtil.showError(this, "Failed to save address: " + ex.getMessage());
            }
        }
    }
}
