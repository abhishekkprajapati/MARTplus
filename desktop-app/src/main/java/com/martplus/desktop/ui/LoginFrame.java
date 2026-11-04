package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.dao.JdbcUserDao;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.AuthenticationService;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login Screen for the MARTplus desktop application.
 * Connects directly to PostgreSQL user records with BCrypt password verification.
 */
public class LoginFrame extends JPanel {

    private final MainWindow mainWindow;
    private final AuthenticationService authService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;

    public LoginFrame(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.authService = new AuthenticationService(new JdbcUserDao());

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        buildUI();
    }

    private void buildUI() {
        // Main Login Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(440, 520));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(36, 40, 36, 40)
        ));

        // Brand Logo and Title
        JPanel brandHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        brandHeader.setBackground(Color.WHITE);

        JLabel logoBadge = new JLabel("M+");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setOpaque(true);
        logoBadge.setBackground(UITheme.INDIGO_ACCENT);
        logoBadge.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel titleText = new JLabel("MARTplus");
        titleText.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleText.setForeground(UITheme.NAVY_PRIMARY);

        brandHeader.add(logoBadge);
        brandHeader.add(titleText);
        card.add(brandHeader);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel subtitle = new JLabel("Desktop Enterprise Portal");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createRigidArea(new Dimension(0, 28)));

        // Email Field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UITheme.FONT_BODY_BOLD);
        emailLabel.setForeground(UITheme.NAVY_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emailLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        emailField = new JTextField();
        emailField.setFont(UITheme.FONT_BODY);
        emailField.setMaximumSize(new Dimension(360, 40));
        emailField.setPreferredSize(new Dimension(360, 40));
        emailField.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emailField);

        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UITheme.FONT_BODY_BOLD);
        passwordLabel.setForeground(UITheme.NAVY_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setMaximumSize(new Dimension(360, 40));
        passwordField.setPreferredSize(new Dimension(360, 40));
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);

        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // Account Type Combo
        JLabel roleLabel = new JLabel("Account Type");
        roleLabel.setFont(UITheme.FONT_BODY_BOLD);
        roleLabel.setForeground(UITheme.NAVY_PRIMARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        roleComboBox = new JComboBox<>(new String[]{"BUYER", "SELLER", "ADMIN"});
        roleComboBox.setFont(UITheme.FONT_BODY);
        roleComboBox.setMaximumSize(new Dimension(360, 40));
        roleComboBox.setPreferredSize(new Dimension(360, 40));
        roleComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleComboBox);

        card.add(Box.createRigidArea(new Dimension(0, 24)));

        // Login Button
        loginButton = UITheme.createPrimaryButton("Sign In to MARTplus");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setMaximumSize(new Dimension(360, 42));
        loginButton.setPreferredSize(new Dimension(360, 42));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());
        card.add(loginButton);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Exit Button
        exitButton = UITheme.createSecondaryButton("Exit Application");
        exitButton.setMaximumSize(new Dimension(360, 38));
        exitButton.setPreferredSize(new Dimension(360, 38));
        exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));
        card.add(exitButton);

        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // Status Feedback Label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);

        // Enter key listener on password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        add(card);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (email.isEmpty()) {
            DialogUtil.showWarning(this, "Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            DialogUtil.showWarning(this, "Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        // Disable button during execution
        setControlsEnabled(false);
        statusLabel.setText("Authenticating credentials with PostgreSQL...");
        statusLabel.setForeground(UITheme.INDIGO_ACCENT);

        AsyncOperation.run(
                () -> authService.login(email, password, role),
                authenticatedUser -> {
                    setControlsEnabled(true);
                    statusLabel.setText("Login successful! Loading dashboard...");
                    statusLabel.setForeground(UITheme.SUCCESS_GREEN);
                    mainWindow.showDashboard(authenticatedUser);
                },
                throwable -> {
                    setControlsEnabled(true);
                    statusLabel.setText("Authentication failed.");
                    statusLabel.setForeground(UITheme.DANGER_RED);
                    DialogUtil.showError(this, throwable.getMessage());
                    passwordField.setText("");
                    passwordField.requestFocus();
                }
        );
    }

    private void setControlsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        exitButton.setEnabled(enabled);
        emailField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        roleComboBox.setEnabled(enabled);
    }

    public void resetFields() {
        passwordField.setText("");
        statusLabel.setText(" ");
        setControlsEnabled(true);
    }
}
