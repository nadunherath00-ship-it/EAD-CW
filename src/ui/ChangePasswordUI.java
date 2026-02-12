package ui;

import dao.UserDAO;
import util.SessionManager;
import exception.DatabaseException;
import exception.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ChangePasswordUI extends JDialog {
    
    private JPasswordField txtCurrentPassword, txtNewPassword, txtConfirmPassword;
    private JButton btnChange, btnCancel;
    private JCheckBox chkShowPassword;
    
    private UserDAO userDAO;
    
    public ChangePasswordUI(JFrame parent) {
        super(parent, "Change Password", true);
        
        try {
            userDAO = new UserDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("Change Password");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 20));
        lblHeader.setForeground(new Color(44, 62, 80));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblUser = new JLabel("User: " + SessionManager.getInstance().getCurrentUsername());
        lblUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUser.setForeground(new Color(149, 165, 166));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(lblHeader);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(lblUser);
        mainPanel.add(Box.createVerticalStrut(25));

        JLabel lblCurrent = new JLabel("Current Password:");
        lblCurrent.setFont(new Font("Arial", Font.BOLD, 12));
        lblCurrent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtCurrentPassword = new JPasswordField();
        txtCurrentPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtCurrentPassword.setMaximumSize(new Dimension(400, 35));
        
        mainPanel.add(lblCurrent);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtCurrentPassword);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel lblNew = new JLabel("New Password:");
        lblNew.setFont(new Font("Arial", Font.BOLD, 12));
        lblNew.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNewPassword.setMaximumSize(new Dimension(400, 35));
        
        mainPanel.add(lblNew);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtNewPassword);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel lblConfirm = new JLabel("Confirm New Password:");
        lblConfirm.setFont(new Font("Arial", Font.BOLD, 12));
        lblConfirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtConfirmPassword.setMaximumSize(new Dimension(400, 35));
        
        mainPanel.add(lblConfirm);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtConfirmPassword);
        mainPanel.add(Box.createVerticalStrut(10));

        chkShowPassword = new JCheckBox("Show Passwords");
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.setFont(new Font("Arial", Font.PLAIN, 11));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e -> {
            char echoChar = chkShowPassword.isSelected() ? (char) 0 : '•';
            txtCurrentPassword.setEchoChar(echoChar);
            txtNewPassword.setEchoChar(echoChar);
            txtConfirmPassword.setEchoChar(echoChar);
        });
        
        mainPanel.add(chkShowPassword);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(400, 45));
        
        btnChange = new JButton("Change Password");
        btnChange.setFont(new Font("Arial", Font.BOLD, 12));
        btnChange.setBackground(new Color(46, 204, 113));
        btnChange.setForeground(Color.WHITE);
        btnChange.setFocusPainted(false);
        btnChange.setBorderPainted(false);
        btnChange.setPreferredSize(new Dimension(150, 35));
        btnChange.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChange.addActionListener(e -> changePassword());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.setBackground(new Color(149, 165, 166));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnChange);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private void changePassword() {
        String currentPassword = new String(txtCurrentPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (currentPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter current password", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtCurrentPassword.requestFocus();
            return;
        }
        
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter new password", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return;
        }
        
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "New password must be at least 6 characters", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "New passwords do not match", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtConfirmPassword.requestFocus();
            return;
        }
        
        if (currentPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(this, 
                "New password must be different from current password", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return;
        }

        try {
            int userId = SessionManager.getInstance().getCurrentUser().getUserId();
            
            if (userDAO.changePassword(userId, currentPassword, newPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Password changed successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            txtCurrentPassword.setText("");
            txtCurrentPassword.requestFocus();
        }
    }
}