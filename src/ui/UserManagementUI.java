package ui;

import dao.UserDAO;
import model.User;
import util.SessionManager;
import exception.DatabaseException;
import exception.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementUI extends JFrame {
    
    private JTextField txtUsername, txtFullName, txtEmail, txtPassword, txtSearch;
    private JComboBox<String> cmbRole, cmbStatus;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnReset, btnClear, btnSearch;
    
    private UserDAO userDAO;
    private int selectedUserId = -1;
    
    public UserManagementUI() {
        
        if (!SessionManager.getInstance().isAdmin()) {
            JOptionPane.showMessageDialog(null, 
                "Access Denied! Admin privileges required.",
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        try {
            userDAO = new UserDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("User Management (Admin)");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadUsers();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("👥 USER MANAGEMENT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        formPanel.add(txtUsername, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 3;
        txtPassword = new JTextField(20);
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(20);
        formPanel.add(txtFullName, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        cmbRole = new JComboBox<>(new String[]{"Admin", "Staff", "User"});
        formPanel.add(cmbRole, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        formPanel.add(cmbStatus, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAdd = createButton("➕ Add User", new Color(46, 204, 113));
        btnUpdate = createButton("✏ Update User", new Color(52, 152, 219));
        btnDelete = createButton("🗑 Delete User", new Color(231, 76, 60));
        btnReset = createButton("🔑 Reset Password", new Color(230, 126, 34));
        btnClear = createButton("🔄 Clear Form", new Color(149, 165, 166));
        
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnReset.addActionListener(e -> resetPassword());
        btnClear.addActionListener(e -> clearForm());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnClear);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        searchPanel.add(lblSearch);
        
        txtSearch = new JTextField(30);
        txtSearch.setPreferredSize(new Dimension(300, 30));
        searchPanel.add(txtSearch);
        
        btnSearch = createButton("🔍 Search", new Color(155, 89, 182));
        btnSearch.addActionListener(e -> searchUsers());
        searchPanel.add(btnSearch);
        
        JButton btnRefresh = createButton("🔄 Refresh", new Color(26, 188, 156));
        btnRefresh.addActionListener(e -> loadUsers());
        searchPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("User List"));
        
        String[] columns = {"ID", "Username", "Full Name", "Email", "Role", "Status", "Last Login"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color buttonColor = color;
                if (!isEnabled()) {
                    buttonColor = new Color(189, 195, 199);
                } else if (getModel().isPressed()) {
                    buttonColor = color.darker().darker();
                } else if (getModel().isRollover()) {
                    buttonColor = color.darker();
                }
                
                g2d.setColor(buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            tableModel.setRowCount(0);
            
            for (User user : users) {
                Object[] row = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    user.getLastLogin() != null ? user.getLastLogin().toString() : "Never"
                };
                tableModel.addRow(row);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedUserId = (int) tableModel.getValueAt(selectedRow, 0);
            
            try {
                User user = userDAO.getUserById(selectedUserId);
                if (user != null) {
                    txtUsername.setText(user.getUsername());
                    txtFullName.setText(user.getFullName());
                    txtEmail.setText(user.getEmail());
                    txtPassword.setText(""); // Don't show password
                    cmbRole.setSelectedItem(user.getRole());
                    cmbStatus.setSelectedItem(user.getStatus());
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addUser() {
        try {
            User user = new User();
            user.setUsername(txtUsername.getText().trim());
            user.setPassword(txtPassword.getText().trim());
            user.setFullName(txtFullName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setRole((String) cmbRole.getSelectedItem());
            user.setStatus((String) cmbStatus.getSelectedItem());
            
            if (userDAO.addUser(user)) {
                JOptionPane.showMessageDialog(this, 
                    "User added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUsers();
            }
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to update", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            User user = new User();
            user.setUserId(selectedUserId);
            user.setUsername(txtUsername.getText().trim());
            user.setFullName(txtFullName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setRole((String) cmbRole.getSelectedItem());
            user.setStatus((String) cmbStatus.getSelectedItem());
            
            if (userDAO.updateUser(user)) {
                JOptionPane.showMessageDialog(this, 
                    "User updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUsers();
            }
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to delete", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (userDAO.deleteUser(selectedUserId)) {
                    JOptionPane.showMessageDialog(this, 
                        "User deleted successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadUsers();
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetPassword() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to reset password", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String newPassword = JOptionPane.showInputDialog(this, 
            "Enter new password (min 6 characters):", 
            "Reset Password", JOptionPane.QUESTION_MESSAGE);
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            try {
                if (userDAO.resetPassword(selectedUserId, newPassword)) {
                    JOptionPane.showMessageDialog(this, 
                        "Password reset successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (ValidationException | DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchUsers() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }
        
        try {
            List<User> users = userDAO.searchUsers(searchTerm);
            tableModel.setRowCount(0);
            
            for (User user : users) {
                Object[] row = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    user.getLastLogin() != null ? user.getLastLogin().toString() : "Never"
                };
                tableModel.addRow(row);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtUsername.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        cmbRole.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        selectedUserId = -1;
        userTable.clearSelection();
    }
}