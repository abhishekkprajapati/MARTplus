package com.martplus.desktop.ui;

import javax.swing.JComponent;

/**
 * Interface defining the contract for role-based Dashboards.
 * Fulfills Phase 3: Polymorphism & Interfaces.
 */
public interface DashboardView {

    /**
     * Initializes the view components and layouts.
     */
    void initView();

    /**
     * Refreshes the dashboard data asynchronously from PostgreSQL.
     */
    void refreshData();

    /**
     * Returns the Swing JComponent to be mounted into the main window.
     */
    JComponent getViewComponent();

    /**
     * Returns the user role associated with this dashboard.
     */
    String getRoleName();

    /**
     * Called when the dashboard is activated/displayed.
     */
    void onActivated();

    /**
     * Called when navigating away or logging out.
     */
    void onDeactivated();
}
