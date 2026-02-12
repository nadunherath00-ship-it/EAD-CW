package ui;

import dao.UserDAO;
import model.User;
import util.SessionManager;
import exception.DatabaseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class LoginUI extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;
    
    private UserDAO userDAO;

    private final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color DARK_TEXT = new Color(44, 62, 80);
    private final Color LIGHT_TEXT = new Color(149, 165, 166);
    private final Color BACKGROUND = new Color(236, 240, 241);
    
    public LoginUI() {
        try {
            userDAO = new UserDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        setTitle("SAMS - Login");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel leftPanel = createLeftPanel();

        JPanel rightPanel = createRightPanel();
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PRIMARY_COLOR);
        leftPanel.setPreferredSize(new Dimension(400, 650));
        leftPanel.setBorder(new EmptyBorder(50, 40, 50, 40));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(PRIMARY_COLOR);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogo = new JLabel("SAMS");
        lblLogo.setFont(new Font("Arial", Font.BOLD, 72));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Smart Academic");
        lblSubtitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblSubtitle.setForeground(new Color(236, 240, 241));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitle2 = new JLabel("Management System");
        lblSubtitle2.setFont(new Font("Arial", Font.BOLD, 24));
        lblSubtitle2.setForeground(new Color(236, 240, 241));
        lblSubtitle2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(20));
        logoPanel.add(lblSubtitle);
        logoPanel.add(lblSubtitle2);
        
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoPanel);
        leftPanel.add(Box.createVerticalStrut(40));

        String[] features = {
            "✓ Student Management",
            "✓ Course Enrollment",
            "✓ Attendance Tracking",
            "✓ Assessment Reports",
            "✓ Analytics Dashboard"
        };
        
        for (String feature : features) {
            JLabel lblFeature = new JLabel(feature);
            lblFeature.setFont(new Font("Arial", Font.PLAIN, 16));
            lblFeature.setForeground(Color.WHITE);
            lblFeature.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblFeature.setBorder(new EmptyBorder(5, 0, 5, 0));
            leftPanel.add(lblFeature);
        }
        
        leftPanel.add(Box.createVerticalGlue());
        
        
        
        return leftPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 32));
        lblWelcome.setForeground(DARK_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rightPanel.add(lblWelcome, gbc);
        
        JLabel lblInstruction = new JLabel("Please login to your account");
        lblInstruction.setFont(new Font("Arial", Font.PLAIN, 15));
        lblInstruction.setForeground(LIGHT_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        rightPanel.add(lblInstruction, gbc);

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsername.setForeground(DARK_TEXT);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 0, 5, 0);
        rightPanel.add(lblUsername, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 15));
        txtUsername.setPreferredSize(new Dimension(350, 45));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        txtUsername.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                txtUsername.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void focusLost(FocusEvent e) {
                txtUsername.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        rightPanel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setForeground(DARK_TEXT);
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 5, 0);
        rightPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 15));
        txtPassword.setPreferredSize(new Dimension(350, 45));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        txtPassword.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                txtPassword.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void focusLost(FocusEvent e) {
                txtPassword.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        rightPanel.add(txtPassword, gbc);

        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        chkShowPassword.setForeground(LIGHT_TEXT);
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(chkShowPassword, gbc);

        btnLogin = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(39, 174, 96));
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(350, 50));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> performLogin());
        
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 0, 15, 0);
        rightPanel.add(btnLogin, gbc);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 13));
        lblStatus.setForeground(DANGER_COLOR);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 0, 10, 0);
        rightPanel.add(lblStatus, gbc);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        infoPanel.setBackground(BACKGROUND);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        

        txtPassword.addActionListener(e -> performLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        
        return rightPanel;
    }
    
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            showError("Please enter username");
            txtUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter password");
            txtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("AUTHENTICATING...");
        lblStatus.setText("Authenticating...");
        lblStatus.setForeground(PRIMARY_COLOR);

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.authenticate(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    
                    if (user != null) {
                        
                        SessionManager.getInstance().login(user);
                        
                        lblStatus.setText("✓ Login successful!");
                        lblStatus.setForeground(SUCCESS_COLOR);
                       
                        Timer timer = new Timer(800, e -> {
                            dispose();
                            new Dashboard().setVisible(true);
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        
                        showError("✗ Invalid username or password");
                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        
                        shakeFrame();
                    }
                } catch (Exception e) {
                    showError("✗ Login error: " + e.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");
                }
            }
        };
        
        worker.execute();
    }
    
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setForeground(DANGER_COLOR);
    }
    
    private void shakeFrame() {
        Point location = getLocation();
        int x = location.x;
        
        Timer timer = new Timer(50, null);
        final int[] count = {0};
        
        timer.addActionListener(e -> {
            if (count[0] < 10) {
                int offset = (count[0] % 2 == 0) ? 10 : -10;
                setLocation(x + offset, location.y);
                count[0]++;
            } else {
                setLocation(x, location.y);
                ((Timer) e.getSource()).stop();
            }
        });
        
        timer.start();
    }
    
    public static void main(String[] args) {
        try {
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}