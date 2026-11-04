package com.martplus.desktop.ui;

import com.martplus.desktop.config.AppConfig;
import com.martplus.desktop.model.User;
import com.martplus.desktop.util.DateUtil;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Base Class for role-specific Dashboards.
 * Demonstrates:
 * 1. OOP Inheritance: BaseDashboard provides unified layout, navigation mechanisms, and lifecycle.
 * 2. Polymorphism: Implements DashboardView interface methods.
 * 3. Encapsulation of common UI state and navigation actions.
 */
public abstract class BaseDashboard extends JPanel implements DashboardView {

    protected final User currentUser;
    protected final MainWindow parentWindow;

    // Navigation and Panels
    protected final JPanel sidebarPanel;
    protected final JPanel mainContentContainer;
    protected final CardLayout contentCardLayout;
    protected final JLabel titleLabel;
    protected final JLabel statusLabel;
    protected final Map<String, JButton> navButtons = new HashMap<>();

    public BaseDashboard(MainWindow parentWindow, User currentUser) {
        this.parentWindow = parentWindow;
        this.currentUser = currentUser != null ? currentUser : AppConfig.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Sidebar (West)
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UITheme.NAVY_SECONDARY);
        sidebarPanel.setPreferredSize(new Dimension(240, 700));
        sidebarPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        // Top Header
        JPanel headerPanel = createHeaderPanel();

        // Central Content Area with CardLayout
        contentCardLayout = new CardLayout();
        mainContentContainer = new JPanel(contentCardLayout);
        mainContentContainer.setBackground(UITheme.BG_LIGHT);
        mainContentContainer.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Bottom Status Bar
        JPanel bottomStatusBar = new JPanel(new BorderLayout());
        bottomStatusBar.setBackground(Color.WHITE);
        bottomStatusBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 16, 6, 16)
        ));
        statusLabel = new JLabel("Connected to PostgreSQL Database | Ready");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        bottomStatusBar.add(statusLabel, BorderLayout.WEST);

        JLabel dbBadge = new JLabel("Direct JDBC: Active");
        dbBadge.setFont(UITheme.FONT_SMALL);
        dbBadge.setForeground(UITheme.SUCCESS_GREEN);
        bottomStatusBar.add(dbBadge, BorderLayout.EAST);

        // Right side wrapper
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(headerPanel, BorderLayout.NORTH);
        rightContainer.add(mainContentContainer, BorderLayout.CENTER);
        rightContainer.add(bottomStatusBar, BorderLayout.SOUTH);

        add(sidebarPanel, BorderLayout.WEST);
        add(rightContainer, BorderLayout.CENTER);

        // Initialize branding, navigation, and views
        buildSidebarHeader();
        buildNavigation(sidebarPanel);
        buildSidebarFooter();

        titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setBackground(Color.WHITE);

        JLabel welcomeMsg = new JLabel("Welcome back, " + (currentUser != null ? currentUser.getFirstName() : "User"));
        welcomeMsg.setFont(UITheme.FONT_TITLE);
        welcomeMsg.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(welcomeMsg);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightHeader.setBackground(Color.WHITE);

        // Role Badge
        String roleText = (currentUser != null ? currentUser.getRole() : getRoleName()).toUpperCase();
        JLabel roleBadge = new JLabel(" " + roleText + " ");
        roleBadge.setFont(UITheme.FONT_BODY_BOLD);
        roleBadge.setForeground(UITheme.INDIGO_ACCENT);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(0xEE, 0xF2, 0xFF));
        roleBadge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.INDIGO_ACCENT, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        rightHeader.add(roleBadge);

        // Refresh Button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UITheme.FONT_BODY);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(UITheme.NAVY_PRIMARY);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());
        rightHeader.add(refreshBtn);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }

    private void buildSidebarHeader() {
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoPanel.setBackground(UITheme.NAVY_SECONDARY);
        logoPanel.setMaximumSize(new Dimension(220, 50));

        JLabel logoBadge = new JLabel("M+");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setOpaque(true);
        logoBadge.setBackground(UITheme.INDIGO_ACCENT);
        logoBadge.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel brandText = new JLabel("MARTplus");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandText.setForeground(Color.WHITE);

        logoPanel.add(logoBadge);
        logoPanel.add(brandText);

        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 24)));
    }

    private void buildSidebarFooter() {
        sidebarPanel.add(Box.createVerticalGlue());

        // User Info Pill
        JPanel userPill = new JPanel(new BorderLayout());
        userPill.setBackground(UITheme.NAVY_PRIMARY);
        userPill.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        userPill.setMaximumSize(new Dimension(220, 60));

        JLabel nameLabel = new JLabel(currentUser != null ? currentUser.getFullName() : "User");
        nameLabel.setFont(UITheme.FONT_BODY_BOLD);
        nameLabel.setForeground(Color.WHITE);

        JLabel emailLabel = new JLabel(currentUser != null ? currentUser.getEmail() : "user@martplus.com");
        emailLabel.setFont(UITheme.FONT_SMALL);
        emailLabel.setForeground(new Color(0x94, 0xA3, 0xB8));

        userPill.add(nameLabel, BorderLayout.NORTH);
        userPill.add(emailLabel, BorderLayout.SOUTH);

        sidebarPanel.add(userPill);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Logout Button
        JButton logoutBtn = UITheme.createDangerButton("Logout");
        logoutBtn.setMaximumSize(new Dimension(220, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());
        sidebarPanel.add(logoutBtn);
    }

    /**
     * Helper to add modern sidebar navigation buttons with selection highlighting.
     */
    protected JButton addNavButton(JPanel panel, String cardName, String title) {
        JButton btn = new JButton(title);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(new Color(0xCB, 0xD5, 0xE1));
        btn.setBackground(UITheme.NAVY_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new EmptyBorder(2, 0, 2, 0),
                new EmptyBorder(10, 14, 10, 14)
        ));
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> selectNavCard(cardName));

        panel.add(btn);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        navButtons.put(cardName, btn);
        return btn;
    }

    public void selectNavCard(String cardName) {
        contentCardLayout.show(mainContentContainer, cardName);

        // Highlight selected nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(cardName)) {
                b.setBackground(UITheme.INDIGO_ACCENT);
                b.setForeground(Color.WHITE);
                b.setFont(UITheme.FONT_BODY_BOLD);
            } else {
                b.setBackground(UITheme.NAVY_SECONDARY);
                b.setForeground(new Color(0xCB, 0xD5, 0xE1));
                b.setFont(UITheme.FONT_BODY);
            }
        }
    }

    protected void handleLogout() {
        if (DialogUtil.confirm(this, "Are you sure you want to log out of MARTplus?", "Confirm Logout")) {
            onDeactivated();
            AppConfig.clearSession();
            parentWindow.showLogin();
        }
    }

    protected void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message + " | Updated: " + DateUtil.formatDateTime(LocalDateTime.now()));
        }
    }

    /**
     * Abstract method implemented by sub-classes to build role-specific navigation menus.
     */
    protected abstract void buildNavigation(JPanel navPanel);

    @Override
    public JComponent getViewComponent() {
        return this;
    }

    @Override
    public void onActivated() {
        refreshData();
    }

    @Override
    public void onDeactivated() {
        // Default cleanup if needed
    }
}
