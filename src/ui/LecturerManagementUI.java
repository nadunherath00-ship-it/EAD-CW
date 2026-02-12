package ui;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LecturerManagementUI extends JFrame {
    
    private JTextField txtLecturerNumber, txtFirstName, txtLastName, txtEmail, 
                       txtPhone, txtDepartment, txtQualification, txtSearch;
    private JTable lecturerTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnRefresh;
    
    private Connection connection;
    private int selectedLecturerId = -1;
    
    public LecturerManagementUI() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Lecturer Management");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadLecturers();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(22, 160, 133));
        JLabel headerLabel = new JLabel("👨‍🏫 LECTURER MANAGEMENT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Lecturer Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Lecturer Number:"), gbc);
        gbc.gridx = 1;
        txtLecturerNumber = new JTextField(15);
        formPanel.add(txtLecturerNumber, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 3;
        txtDepartment = new JTextField(20);
        formPanel.add(txtDepartment, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        txtFirstName = new JTextField(15);
        formPanel.add(txtFirstName, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3;
        txtLastName = new JTextField(20);
        formPanel.add(txtLastName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        formPanel.add(txtEmail, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 3;
        txtPhone = new JTextField(20);
        formPanel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Qualification:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtQualification = new JTextField();
        formPanel.add(txtQualification, gbc);
        gbc.gridwidth = 1;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAdd = createButton("➕ Add Lecturer", new Color(46, 204, 113));
        btnUpdate = createButton("✏ Update Lecturer", new Color(52, 152, 219));
        btnDelete = createButton("🗑 Delete Lecturer", new Color(231, 76, 60));
        btnClear = createButton("🔄 Clear Form", new Color(149, 165, 166));
        
        btnAdd.addActionListener(e -> addLecturer());
        btnUpdate.addActionListener(e -> updateLecturer());
        btnDelete.addActionListener(e -> deleteLecturer());
        btnClear.addActionListener(e -> clearForm());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
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
        btnSearch.addActionListener(e -> searchLecturers());
        searchPanel.add(btnSearch);
        
        btnRefresh = createButton("🔄 Refresh", new Color(26, 188, 156));
        btnRefresh.addActionListener(e -> loadLecturers());
        searchPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Lecturer List"));
        
        String[] columns = {"ID", "Lecturer Number", "Name", "Email", "Phone", 
                           "Department", "Qualification"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lecturerTable = new JTable(tableModel);
        lecturerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lecturerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedLecturer();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(lecturerTable);
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
        
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadLecturers() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM lecturers ORDER BY lecturer_number";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("lecturer_id"),
                    rs.getString("lecturer_number"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getString("qualification")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading lecturers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedLecturer() {
        int selectedRow = lecturerTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedLecturerId = (int) tableModel.getValueAt(selectedRow, 0);
            
            try {
                String query = "SELECT * FROM lecturers WHERE lecturer_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, selectedLecturerId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    txtLecturerNumber.setText(rs.getString("lecturer_number"));
                    txtFirstName.setText(rs.getString("first_name"));
                    txtLastName.setText(rs.getString("last_name"));
                    txtEmail.setText(rs.getString("email"));
                    txtPhone.setText(rs.getString("phone"));
                    txtDepartment.setText(rs.getString("department"));
                    txtQualification.setText(rs.getString("qualification"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading lecturer: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addLecturer() {
        try {
            String query = "INSERT INTO lecturers (lecturer_number, first_name, last_name, " +
                          "email, phone, department, qualification) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, txtLecturerNumber.getText().trim());
            stmt.setString(2, txtFirstName.getText().trim());
            stmt.setString(3, txtLastName.getText().trim());
            stmt.setString(4, txtEmail.getText().trim());
            stmt.setString(5, txtPhone.getText().trim());
            stmt.setString(6, txtDepartment.getText().trim());
            stmt.setString(7, txtQualification.getText().trim());
            
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Lecturer added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadLecturers();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding lecturer: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateLecturer() {
        if (selectedLecturerId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a lecturer to update",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String query = "UPDATE lecturers SET lecturer_number=?, first_name=?, last_name=?, " +
                          "email=?, phone=?, department=?, qualification=? WHERE lecturer_id=?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, txtLecturerNumber.getText().trim());
            stmt.setString(2, txtFirstName.getText().trim());
            stmt.setString(3, txtLastName.getText().trim());
            stmt.setString(4, txtEmail.getText().trim());
            stmt.setString(5, txtPhone.getText().trim());
            stmt.setString(6, txtDepartment.getText().trim());
            stmt.setString(7, txtQualification.getText().trim());
            stmt.setInt(8, selectedLecturerId);
            
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Lecturer updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadLecturers();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating lecturer: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteLecturer() {
        if (selectedLecturerId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a lecturer to delete",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this lecturer?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM lecturers WHERE lecturer_id=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, selectedLecturerId);
                
                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Lecturer deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadLecturers();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting lecturer: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchLecturers() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadLecturers();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM lecturers WHERE lecturer_number LIKE ? OR " +
                          "first_name LIKE ? OR last_name LIKE ? ORDER BY lecturer_number";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            String search = "%" + searchTerm + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("lecturer_id"),
                    rs.getString("lecturer_number"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getString("qualification")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching lecturers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtLecturerNumber.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtDepartment.setText("");
        txtQualification.setText("");
        selectedLecturerId = -1;
        lecturerTable.clearSelection();
    }
}