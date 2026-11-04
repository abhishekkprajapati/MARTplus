package com.martplus.desktop.ui;

import com.martplus.desktop.config.AppConfig;
import com.martplus.desktop.model.User;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window hosting the Login screen and dynamic role Dashboards.
 * Demonstrates polymorphic dashboard mounting via DashboardView.
 */
public class MainWindow extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private LoginFrame loginPanel;
    private DashboardView currentDashboardView;

    public MainWindow() {
        setTitle("MARTplus — Multi-Vendor E-Commerce Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setBackground(UITheme.BG_LIGHT);

        loginPanel = new LoginFrame(this);
        containerPanel.add(loginPanel, "LOGIN");

        add(containerPanel);
        showLogin();
    }

    public void showLogin() {
        if (currentDashboardView != null) {
            currentDashboardView.onDeactivated();
            containerPanel.remove(currentDashboardView.getViewComponent());
            currentDashboardView = null;
        }
        cardLayout.show(containerPanel, "LOGIN");
        loginPanel.resetFields();
    }

    public void showDashboard(User user) {
        // Demonstrate Polymorphism via RoleDashboardFactory
        DashboardView dashboardView = RoleDashboardFactory.createDashboard(user, this);
        this.currentDashboardView = dashboardView;

        containerPanel.add(dashboardView.getViewComponent(), "DASHBOARD");
        cardLayout.show(containerPanel, "DASHBOARD");
        dashboardView.onActivated();
    }
}
