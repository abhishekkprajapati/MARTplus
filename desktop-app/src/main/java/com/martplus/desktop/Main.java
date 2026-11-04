package com.martplus.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import com.martplus.desktop.config.DatabaseConfig;
import com.martplus.desktop.ui.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Entry Point for the MARTplus Java Swing Desktop Application.
 *
 * Demonstrates:
 * 1. Java 21 Swing initialization on the Event Dispatch Thread (EDT).
 * 2. Modern Look and Feel (FlatLaf) adhering to MARTplus brand identity.
 * 3. PostgreSQL database configuration and pre-flight connectivity verification.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   MARTplus Desktop Application (Java Swing + JDBC)   ");
        System.out.println("=================================================");

        // Configure modern FlatLaf styling
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Global UI customizations
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
        } catch (Exception e) {
            System.err.println("Notice: FlatLaf look and feel could not be initialized, defaulting to system L&F: " + e.getMessage());
        }

        // Pre-flight database check
        System.out.println("Target Database: " + DatabaseConfig.getDbUrl());
        System.out.println("Database User:   " + DatabaseConfig.getDbUser());

        boolean connected = DatabaseConfig.testConnection();
        if (connected) {
            System.out.println("✓ PostgreSQL JDBC Connection verified successfully!");
        } else {
            System.out.println("⚠️ Warning: Could not establish immediate connection to PostgreSQL.");
            System.out.println("   Please ensure PostgreSQL is running or configure DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD.");
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                System.out.println("✓ MARTplus Desktop UI launched on EDT.");
            } catch (Exception ex) {
                System.err.println("Fatal error launching GUI: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
