package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.CartItem;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.CartService;
import com.martplus.desktop.service.ProductService;
import com.martplus.desktop.service.WishlistService;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Panel for browsing products, searching, category filtering, and adding to cart/wishlist.
 * Demonstrates:
 * 1. JTable for PostgreSQL database records.
 * 2. AsyncOperation off the EDT to keep Swing UI responsive.
 * 3. Collections (List<Product>, Set<String> categories).
 */
public class ProductPanel extends JPanel {

    private final ProductService productService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final User currentUser;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JButton addToCartBtn;
    private JButton addToWishlistBtn;
    private List<Product> currentProductList = new ArrayList<>();

    public ProductPanel(ProductService productService, CartService cartService,
                        WishlistService wishlistService, User currentUser) {
        this.productService = productService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTopFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        loadCategories();
        loadProducts();
    }

    private JPanel createTopFilterPanel() {
        JPanel filterCard = UITheme.createCard();
        filterCard.setLayout(new BorderLayout());

        JPanel inputsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        inputsPanel.setBackground(Color.WHITE);

        inputsPanel.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.addActionListener(e -> loadProducts());
        inputsPanel.add(searchField);

        inputsPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"All"});
        categoryCombo.setFont(UITheme.FONT_BODY);
        categoryCombo.addActionListener(e -> loadProducts());
        inputsPanel.add(categoryCombo);

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> loadProducts());
        inputsPanel.add(searchBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (categoryCombo.getItemCount() > 0) {
                categoryCombo.setSelectedIndex(0);
            }
            loadProducts();
        });
        inputsPanel.add(resetBtn);

        filterCard.add(inputsPanel, BorderLayout.WEST);
        return filterCard;
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        String[] columns = {"ID", "Product Name", "Category", "Price (₹)", "Stock", "Status", "Seller"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UITheme.styleTable(productTable);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Adjust column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel bottomCard = UITheme.createCard();
        bottomCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        addToWishlistBtn = UITheme.createSecondaryButton("♡ Add to Wishlist");
        addToWishlistBtn.addActionListener(e -> handleAddToWishlist());
        bottomCard.add(addToWishlistBtn);

        addToCartBtn = UITheme.createPrimaryButton("🛒 Add to Cart");
        addToCartBtn.addActionListener(e -> handleAddToCart());
        bottomCard.add(addToCartBtn);

        JButton detailsBtn = UITheme.createSecondaryButton("View Details");
        detailsBtn.addActionListener(e -> showProductDetails());
        bottomCard.add(detailsBtn);

        return bottomCard;
    }

    public void loadCategories() {
        AsyncOperation.run(
                productService::getUniqueCategoryNames,
                categories -> {
                    categoryCombo.removeAllItems();
                    for (String cat : categories) {
                        categoryCombo.addItem(cat);
                    }
                },
                ex -> System.err.println("Failed to load categories: " + ex.getMessage())
        );
    }

    public void loadProducts() {
        String keyword = searchField.getText().trim();
        String selectedCategory = (String) categoryCombo.getSelectedItem();

        AsyncOperation.run(
                () -> productService.searchProducts(keyword, selectedCategory),
                products -> {
                    currentProductList = products;
                    tableModel.setRowCount(0);
                    for (Product p : products) {
                        tableModel.addRow(new Object[]{
                                p.getId(),
                                p.getName(),
                                p.getCategory(),
                                String.format("%.2f", p.getPrice()),
                                p.getStock(),
                                p.getStatus(),
                                p.getSellerName() != null ? p.getSellerName() : "Seller #" + p.getSellerId()
                        });
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load products: " + ex.getMessage())
        );
    }

    private void handleAddToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table first.");
            return;
        }

        Product selected = currentProductList.get(selectedRow);
        if (!selected.isAvailable()) {
            DialogUtil.showWarning(this, "This product is currently out of stock or inactive.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter quantity for '" + selected.getName() + "':", "1");
        if (input == null || input.isBlank()) return;

        try {
            int qty = Integer.parseInt(input.trim());
            if (qty <= 0) {
                DialogUtil.showWarning(this, "Quantity must be at least 1.");
                return;
            }

            addToCartBtn.setEnabled(false);
            AsyncOperation.run(
                    () -> {
                        cartService.addToCart(currentUser.getId(), selected.getId(), qty);
                        return null;
                    },
                    res -> {
                        DialogUtil.showSuccess(this, "Added " + qty + " item(s) to your cart!");
                        addToCartBtn.setEnabled(true);
                    },
                    ex -> {
                        DialogUtil.showError(this, ex.getMessage());
                        addToCartBtn.setEnabled(true);
                    }
            );
        } catch (NumberFormatException nfe) {
            DialogUtil.showWarning(this, "Please enter a valid numeric quantity.");
        }
    }

    private void handleAddToWishlist() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table first.");
            return;
        }

        Product selected = currentProductList.get(selectedRow);
        addToWishlistBtn.setEnabled(false);

        AsyncOperation.run(
                () -> {
                    wishlistService.addToWishlist(currentUser.getId(), selected.getId());
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Added to wishlist: " + selected.getName());
                    addToWishlistBtn.setEnabled(true);
                },
                ex -> {
                    DialogUtil.showError(this, ex.getMessage());
                    addToWishlistBtn.setEnabled(true);
                }
        );
    }

    private void showProductDetails() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarning(this, "Please select a product from the table first.");
            return;
        }

        Product p = currentProductList.get(selectedRow);
        String details = "Product: " + p.getName() + "\n" +
                "Category: " + p.getCategory() + "\n" +
                "Price: ₹" + p.getPrice() + "\n" +
                "Available Stock: " + p.getStock() + "\n" +
                "Status: " + p.getStatus() + "\n" +
                "Seller: " + (p.getSellerName() != null ? p.getSellerName() : "ID #" + p.getSellerId()) + "\n\n" +
                "Description:\n" + (p.getDescription() != null && !p.getDescription().isBlank() ? p.getDescription() : "No description provided.");

        JTextArea area = new JTextArea(details);
        area.setEditable(false);
        area.setFont(UITheme.FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 260));

        JOptionPane.showMessageDialog(this, scroll, "Product Details - " + p.getName(), JOptionPane.INFORMATION_MESSAGE);
    }
}
