package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.User;
import com.martplus.desktop.model.WishlistItem;
import com.martplus.desktop.service.CartService;
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

/**
 * Panel displaying saved Wishlist items for the buyer.
 */
public class WishlistPanel extends JPanel {

    private final WishlistService wishlistService;
    private final CartService cartService;
    private final User currentUser;

    private JTable wishlistTable;
    private DefaultTableModel tableModel;
    private List<WishlistItem> items = new ArrayList<>();

    public WishlistPanel(WishlistService wishlistService, CartService cartService, User currentUser) {
        this.wishlistService = wishlistService;
        this.cartService = cartService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadWishlist();
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Your Saved Wishlist");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Product Name", "Category", "Price (₹)", "Stock", "Availability"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        wishlistTable = new JTable(tableModel);
        UITheme.styleTable(wishlistTable);
        wishlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(wishlistTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton moveToCartBtn = UITheme.createPrimaryButton("🛒 Move to Cart");
        moveToCartBtn.addActionListener(e -> handleMoveToCart());
        card.add(moveToCartBtn);

        JButton removeBtn = UITheme.createDangerButton("Remove from Wishlist");
        removeBtn.addActionListener(e -> handleRemove());
        card.add(removeBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadWishlist());
        card.add(refreshBtn);

        return card;
    }

    public void loadWishlist() {
        AsyncOperation.run(
                () -> wishlistService.getWishlistItems(currentUser.getId()),
                list -> {
                    items = list;
                    tableModel.setRowCount(0);
                    for (WishlistItem w : list) {
                        Product p = w.getProduct();
                        String name = p != null ? p.getName() : "Product #" + w.getProductId();
                        String cat = p != null ? p.getCategory() : "-";
                        BigDecimal price = p != null ? p.getPrice() : BigDecimal.ZERO;
                        int stock = p != null ? p.getStock() : 0;
                        String status = (p != null && p.isAvailable()) ? "In Stock" : "Out of Stock";

                        tableModel.addRow(new Object[]{
                                w.getId(),
                                name,
                                cat,
                                String.format("%.2f", price),
                                stock,
                                status
                        });
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load wishlist: " + ex.getMessage())
        );
    }

    private void handleMoveToCart() {
        int row = wishlistTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an item in your wishlist.");
            return;
        }

        WishlistItem item = items.get(row);
        Product p = item.getProduct();
        if (p == null || !p.isAvailable()) {
            DialogUtil.showWarning(this, "This product is currently out of stock.");
            return;
        }

        AsyncOperation.run(
                () -> {
                    cartService.addToCart(currentUser.getId(), item.getProductId(), 1);
                    wishlistService.removeFromWishlist(item.getId());
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Product moved to your shopping cart!");
                    loadWishlist();
                },
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }

    private void handleRemove() {
        int row = wishlistTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select an item in your wishlist.");
            return;
        }

        WishlistItem item = items.get(row);
        AsyncOperation.run(
                () -> {
                    wishlistService.removeFromWishlist(item.getId());
                    return null;
                },
                res -> loadWishlist(),
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }
}
