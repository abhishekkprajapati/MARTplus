package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Product listing, creation, modification, and status toggling.
 * Enforces ownership rules: Sellers manage their listings; Admins manage all listings.
 */
public class ProductManagementPanel extends JPanel {

    private final ProductService productService;
    private final User currentUser;
    private final boolean isAdminView;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private List<Product> productList = new ArrayList<>();

    public ProductManagementPanel(ProductService productService, User currentUser, boolean isAdminView) {
        this.productService = productService;
        this.currentUser = currentUser;
        this.isAdminView = isAdminView;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        loadProducts();
    }

    private JPanel createTopPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        String title = isAdminView ? "All Product Listings (Admin Catalog)" : "My Product Listings";
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        card.add(titleLbl);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadProducts());
        card.add(refreshBtn);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        String[] cols = {"ID", "Product Name", "Category", "Price (₹)", "Stock", "Status", "Seller ID"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UITheme.styleTable(productTable);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton addBtn = UITheme.createPrimaryButton("+ Add New Product");
        addBtn.addActionListener(e -> handleAddProduct());
        card.add(addBtn);

        JButton editBtn = UITheme.createSecondaryButton("Edit Product");
        editBtn.addActionListener(e -> handleEditProduct());
        card.add(editBtn);

        JButton toggleBtn = UITheme.createSecondaryButton("Toggle Status (Active/Inactive)");
        toggleBtn.addActionListener(e -> handleToggleStatus());
        card.add(toggleBtn);

        JButton deleteBtn = UITheme.createDangerButton("Delete Product");
        deleteBtn.addActionListener(e -> handleDeleteProduct());
        card.add(deleteBtn);

        return card;
    }

    public void loadProducts() {
        AsyncOperation.run(
                () -> isAdminView ? productService.getAllProductsForAdmin(currentUser)
                        : productService.getProductsBySeller(currentUser.getId()),
                list -> {
                    productList = list;
                    tableModel.setRowCount(0);
                    for (Product p : list) {
                        tableModel.addRow(new Object[]{
                                p.getId(),
                                p.getName(),
                                p.getCategory(),
                                String.format("%.2f", p.getPrice()),
                                p.getStock(),
                                p.getStatus(),
                                p.getSellerId() != null ? p.getSellerId() : "-"
                        });
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load products: " + ex.getMessage())
        );
    }

    private void handleAddProduct() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField categoryField = new JTextField("Electronics");
        JTextField imageField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Description:"));
        form.add(descField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Price (₹):"));
        form.add(priceField);
        form.add(new JLabel("Initial Stock:"));
        form.add(stockField);
        form.add(new JLabel("Image URL (optional):"));
        form.add(imageField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Product Listing",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());

                AsyncOperation.run(
                        () -> productService.createProduct(currentUser,
                                nameField.getText().trim(),
                                descField.getText().trim(),
                                price,
                                stock,
                                categoryField.getText().trim(),
                                imageField.getText().trim()
                        ),
                        saved -> {
                            DialogUtil.showSuccess(this, "Product '" + saved.getName() + "' created successfully!");
                            loadProducts();
                        },
                        ex -> DialogUtil.showError(this, ex.getMessage())
                );
            } catch (Exception ex) {
                DialogUtil.showError(this, "Invalid numeric input: " + ex.getMessage());
            }
        }
    }

    private void handleEditProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table to edit.");
            return;
        }

        Product p = productList.get(row);
        JTextField nameField = new JTextField(p.getName());
        JTextField descField = new JTextField(p.getDescription() != null ? p.getDescription() : "");
        JTextField priceField = new JTextField(p.getPrice().toPlainString());
        JTextField stockField = new JTextField(String.valueOf(p.getStock()));
        JTextField categoryField = new JTextField(p.getCategory() != null ? p.getCategory() : "");
        JTextField imageField = new JTextField(p.getImageUrl() != null ? p.getImageUrl() : "");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Description:"));
        form.add(descField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Price (₹):"));
        form.add(priceField);
        form.add(new JLabel("Stock:"));
        form.add(stockField);
        form.add(new JLabel("Image URL:"));
        form.add(imageField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Product #" + p.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());

                AsyncOperation.run(
                        () -> {
                            productService.updateProduct(currentUser, p.getId(),
                                    nameField.getText().trim(),
                                    descField.getText().trim(),
                                    price,
                                    stock,
                                    categoryField.getText().trim(),
                                    imageField.getText().trim()
                            );
                            return null;
                        },
                        res -> {
                            DialogUtil.showSuccess(this, "Product updated successfully!");
                            loadProducts();
                        },
                        ex -> DialogUtil.showError(this, ex.getMessage())
                );
            } catch (Exception ex) {
                DialogUtil.showError(this, "Invalid numeric input: " + ex.getMessage());
            }
        }
    }

    private void handleToggleStatus() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table.");
            return;
        }

        Product p = productList.get(row);
        String nextStatus = "ACTIVE".equalsIgnoreCase(p.getStatus()) ? "INACTIVE" : "ACTIVE";

        AsyncOperation.run(
                () -> {
                    productService.toggleProductStatus(currentUser, p.getId(), nextStatus);
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Product status updated to " + nextStatus);
                    loadProducts();
                },
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }

    private void handleDeleteProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table.");
            return;
        }

        Product p = productList.get(row);
        if (DialogUtil.confirm(this, "Permanently delete product '" + p.getName() + "'?", "Confirm Deletion")) {
            AsyncOperation.run(
                    () -> {
                        productService.deleteProduct(currentUser, p.getId());
                        return null;
                    },
                    res -> {
                        DialogUtil.showSuccess(this, "Product deleted.");
                        loadProducts();
                    },
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        }
    }
}
