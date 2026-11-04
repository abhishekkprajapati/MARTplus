package com.martplus.desktop.ui;

import com.martplus.desktop.concurrency.AsyncOperation;
import com.martplus.desktop.model.User;
import com.martplus.desktop.service.UserService;
import com.martplus.desktop.util.DateUtil;
import com.martplus.desktop.util.DialogUtil;
import com.martplus.desktop.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Administrative User Management.
 * Security: Password hashes are strictly concealed and never displayed in GUI.
 */
public class UserManagementPanel extends JPanel {

    private final UserService userService;
    private final User currentUser;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> roleFilterCombo;
    private List<User> userList = new ArrayList<>();

    public UserManagementPanel(UserService userService, User currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);

        add(createTopFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        loadUsers();
    }

    private JPanel createTopFilterPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JLabel titleLbl = new JLabel("System User Accounts");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        card.add(titleLbl);

        card.add(Box.createRigidArea(new Dimension(20, 0)));
        card.add(new JLabel("Filter Role:"));

        roleFilterCombo = new JComboBox<>(new String[]{"ALL", "BUYER", "SELLER", "ADMIN"});
        roleFilterCombo.setFont(UITheme.FONT_BODY);
        roleFilterCombo.addActionListener(e -> loadUsers());
        card.add(roleFilterCombo);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers());
        card.add(refreshBtn);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        // Note: Password hashes are never exposed in user management table
        String[] cols = {"User ID", "Full Name", "Email", "Phone", "Role", "Status", "Email Verified", "Created On"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        UITheme.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(7).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        JButton addUserBtn = UITheme.createPrimaryButton("+ Create New User");
        addUserBtn.addActionListener(e -> handleCreateUser());
        card.add(addUserBtn);

        JButton changeRoleBtn = UITheme.createSecondaryButton("Change Role");
        changeRoleBtn.addActionListener(e -> handleChangeRole());
        card.add(changeRoleBtn);

        JButton toggleStatusBtn = UITheme.createDangerButton("Toggle Active / Inactive");
        toggleStatusBtn.addActionListener(e -> handleToggleStatus());
        card.add(toggleStatusBtn);

        return card;
    }

    public void loadUsers() {
        String role = (String) roleFilterCombo.getSelectedItem();
        AsyncOperation.run(
                () -> userService.getUsersByRole(currentUser, role),
                list -> {
                    userList = list;
                    tableModel.setRowCount(0);
                    for (User u : list) {
                        tableModel.addRow(new Object[]{
                                u.getId(),
                                u.getFullName(),
                                u.getEmail(),
                                u.getPhone() != null ? u.getPhone() : "-",
                                u.getRole(),
                                u.getStatus(),
                                u.isEmailVerified() ? "Yes" : "No",
                                DateUtil.formatDateOnly(u.getCreatedAt())
                        });
                    }
                },
                ex -> DialogUtil.showError(this, "Failed to load users: " + ex.getMessage())
        );
    }

    private void handleCreateUser() {
        JTextField firstField = new JTextField();
        JTextField lastField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"BUYER", "SELLER", "ADMIN"});

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("First Name:"));
        form.add(firstField);
        form.add(new JLabel("Last Name:"));
        form.add(lastField);
        form.add(new JLabel("Email Address:"));
        form.add(emailField);
        form.add(new JLabel("Phone (10 digits):"));
        form.add(phoneField);
        form.add(new JLabel("Initial Password:"));
        form.add(passField);
        form.add(new JLabel("Assigned Role:"));
        form.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Create System User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pass = new String(passField.getPassword());
            AsyncOperation.run(
                    () -> userService.createUser(currentUser,
                            firstField.getText().trim(),
                            lastField.getText().trim(),
                            emailField.getText().trim(),
                            phoneField.getText().trim(),
                            pass,
                            (String) roleCombo.getSelectedItem()
                    ),
                    u -> {
                        DialogUtil.showSuccess(this, "User " + u.getEmail() + " created successfully!");
                        loadUsers();
                    },
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        }
    }

    private void handleChangeRole() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a user from the table.");
            return;
        }

        User selected = userList.get(row);
        String[] roles = {"BUYER", "SELLER", "ADMIN"};
        String newRole = (String) JOptionPane.showInputDialog(this,
                "Select new role for user " + selected.getEmail() + ":",
                "Change User Role", JOptionPane.QUESTION_MESSAGE, null, roles, selected.getRole());

        if (newRole == null || newRole.equalsIgnoreCase(selected.getRole())) return;

        AsyncOperation.run(
                () -> {
                    userService.updateUserRole(currentUser, selected.getId(), newRole);
                    return null;
                },
                res -> {
                    DialogUtil.showSuccess(this, "Updated role for " + selected.getEmail() + " to " + newRole);
                    loadUsers();
                },
                ex -> DialogUtil.showError(this, ex.getMessage())
        );
    }

    private void handleToggleStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a user from the table.");
            return;
        }

        User selected = userList.get(row);
        String nextStatus = "ACTIVE".equalsIgnoreCase(selected.getStatus()) ? "INACTIVE" : "ACTIVE";

        if (DialogUtil.confirm(this,
                "Change status of user " + selected.getEmail() + " to " + nextStatus + "?",
                "Confirm Status Change")) {
            AsyncOperation.run(
                    () -> {
                        userService.updateUserStatus(currentUser, selected.getId(), nextStatus);
                        return null;
                    },
                    res -> {
                        DialogUtil.showSuccess(this, "User status updated to " + nextStatus);
                        loadUsers();
                    },
                    ex -> DialogUtil.showError(this, ex.getMessage())
            );
        }
    }
}
