package ui;

import util.DatabaseConnection;
import exception.DatabaseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseManagementUI extends JFrame {
    
    private JTextField txtCourseCode, txtCourseName, txtCredits, txtSemester, 
                       txtCapacity, txtDescription, txtSearch;
    private JComboBox<LecturerItem> cmbLecturer;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnRefresh;
    
    private Connection connection;
    private int selectedCourseId = -1;
    
    public CourseManagementUI() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Course Management");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadLecturers();
        loadCourses();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel headerLabel = new JLabel("📚 COURSE MANAGEMENT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Course Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        txtCourseCode = new JTextField(15);
        formPanel.add(txtCourseCode, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 3;
        txtCourseName = new JTextField(25);
        formPanel.add(txtCourseName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        txtCredits = new JTextField(15);
        formPanel.add(txtCredits, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3;
        txtSemester = new JTextField(25);
        formPanel.add(txtSemester, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Assigned Lecturer:"), gbc);
        gbc.gridx = 1;
        cmbLecturer = new JComboBox<>();
        formPanel.add(cmbLecturer, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 3;
        txtCapacity = new JTextField(25);
        formPanel.add(txtCapacity, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDescription = new JTextField();
        formPanel.add(txtDescription, gbc);
        gbc.gridwidth = 1;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAdd = createButton("➕ Add Course", new Color(46, 204, 113));
        btnUpdate = createButton("✏ Update Course", new Color(52, 152, 219));
        btnDelete = createButton("🗑 Delete Course", new Color(231, 76, 60));
        btnClear = createButton("🔄 Clear Form", new Color(149, 165, 166));
        
        btnAdd.addActionListener(e -> addCourse());
        btnUpdate.addActionListener(e -> updateCourse());
        btnDelete.addActionListener(e -> deleteCourse());
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
        btnSearch.addActionListener(e -> searchCourses());
        searchPanel.add(btnSearch);
        
        btnRefresh = createButton("🔄 Refresh", new Color(26, 188, 156));
        btnRefresh.addActionListener(e -> loadCourses());
        searchPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Course List"));
        
        String[] columns = {"ID", "Course Code", "Course Name", "Credits", 
                           "Semester", "Lecturer", "Capacity", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedCourse();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
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
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadLecturers() {
        try {
            cmbLecturer.removeAllItems();
            cmbLecturer.addItem(new LecturerItem(0, "-- No Lecturer --"));
            
            String query = "SELECT lecturer_id, CONCAT(first_name, ' ', last_name) as name " +
                          "FROM lecturers ORDER BY first_name";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                cmbLecturer.addItem(new LecturerItem(
                    rs.getInt("lecturer_id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading lecturers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCourses() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT c.*, CONCAT(l.first_name, ' ', l.last_name) as lecturer_name " +
                          "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.lecturer_id " +
                          "ORDER BY c.course_code";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credits"),
                    rs.getString("semester"),
                    rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Not Assigned",
                    rs.getInt("capacity"),
                    rs.getString("description")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading courses: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedCourseId = (int) tableModel.getValueAt(selectedRow, 0);
            txtCourseCode.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtCourseName.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtCredits.setText(String.valueOf(tableModel.getValueAt(selectedRow, 3)));
            txtSemester.setText((String) tableModel.getValueAt(selectedRow, 4));
            txtCapacity.setText(String.valueOf(tableModel.getValueAt(selectedRow, 6)));
            txtDescription.setText((String) tableModel.getValueAt(selectedRow, 7));
        }
    }
    
    private void addCourse() {
        try {
            String query = "INSERT INTO courses (course_code, course_name, credits, semester, " +
                          "lecturer_id, capacity, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, txtCourseCode.getText().trim());
            stmt.setString(2, txtCourseName.getText().trim());
            stmt.setInt(3, Integer.parseInt(txtCredits.getText().trim()));
            stmt.setString(4, txtSemester.getText().trim());
            
            LecturerItem lecturer = (LecturerItem) cmbLecturer.getSelectedItem();
            if (lecturer.id > 0) {
                stmt.setInt(5, lecturer.id);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setInt(6, Integer.parseInt(txtCapacity.getText().trim()));
            stmt.setString(7, txtDescription.getText().trim());
            
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Course added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadCourses();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for Credits and Capacity",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding course: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCourse() {
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course to update",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String query = "UPDATE courses SET course_code=?, course_name=?, credits=?, " +
                          "semester=?, lecturer_id=?, capacity=?, description=? WHERE course_id=?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, txtCourseCode.getText().trim());
            stmt.setString(2, txtCourseName.getText().trim());
            stmt.setInt(3, Integer.parseInt(txtCredits.getText().trim()));
            stmt.setString(4, txtSemester.getText().trim());
            
            LecturerItem lecturer = (LecturerItem) cmbLecturer.getSelectedItem();
            if (lecturer.id > 0) {
                stmt.setInt(5, lecturer.id);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setInt(6, Integer.parseInt(txtCapacity.getText().trim()));
            stmt.setString(7, txtDescription.getText().trim());
            stmt.setInt(8, selectedCourseId);
            
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Course updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadCourses();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for Credits and Capacity",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating course: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteCourse() {
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course to delete",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this course?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM courses WHERE course_id=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, selectedCourseId);
                
                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Course deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCourses();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting course: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchCourses() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadCourses();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT c.*, CONCAT(l.first_name, ' ', l.last_name) as lecturer_name " +
                          "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.lecturer_id " +
                          "WHERE c.course_code LIKE ? OR c.course_name LIKE ? " +
                          "ORDER BY c.course_code";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            String search = "%" + searchTerm + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credits"),
                    rs.getString("semester"),
                    rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Not Assigned",
                    rs.getInt("capacity"),
                    rs.getString("description")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching courses: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtCourseCode.setText("");
        txtCourseName.setText("");
        txtCredits.setText("");
        txtSemester.setText("");
        txtCapacity.setText("");
        txtDescription.setText("");
        cmbLecturer.setSelectedIndex(0);
        selectedCourseId = -1;
        courseTable.clearSelection();
    }
    
    class LecturerItem {
        int id;
        String name;
        
        LecturerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}