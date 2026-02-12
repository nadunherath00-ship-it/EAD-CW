package ui;

import dao.StudentDAO;
import model.Student;
import exception.DatabaseException;
import exception.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StudentManagementUI extends JFrame {
    
    private JTextField txtStudentNumber, txtFirstName, txtLastName, txtEmail, txtPhone, txtSearch;
    private JComboBox<String> cmbStatus;
    private JFormattedTextField txtDOB, txtEnrollmentDate;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    
    private StudentDAO studentDAO;
    private int selectedStudentId = -1;
    
    public StudentManagementUI() {
        try {
            studentDAO = new StudentDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Student Management");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadStudents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        JLabel headerLabel = new JLabel("STUDENT MANAGEMENT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Student Number:"), gbc);
        gbc.gridx = 1;
        txtStudentNumber = new JTextField(20);
        formPanel.add(txtStudentNumber, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive", "Graduated"});
        formPanel.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        txtFirstName = new JTextField(20);
        formPanel.add(txtFirstName, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3;
        txtLastName = new JTextField(20);
        formPanel.add(txtLastName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 3;
        txtPhone = new JTextField(20);
        formPanel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Date of Birth (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        txtDOB = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtDOB.setColumns(20);
        formPanel.add(txtDOB, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Enrollment Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 3;
        txtEnrollmentDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtEnrollmentDate.setColumns(20);
        txtEnrollmentDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        formPanel.add(txtEnrollmentDate, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAdd = createButton("➕ Add Student", new Color(46, 204, 113));
        btnUpdate = createButton("✏ Update Student", new Color(52, 152, 219));
        btnDelete = createButton("🗑 Delete Student", new Color(231, 76, 60));
        btnClear = createButton("🔄 Clear Form", new Color(149, 165, 166));
        
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
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
        btnSearch.addActionListener(e -> searchStudents());
        searchPanel.add(btnSearch);
        
        JButton btnRefresh = createButton("🔄 Refresh", new Color(26, 188, 156));
        btnRefresh.addActionListener(e -> loadStudents());
        searchPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Student List"));
        
        String[] columns = {"ID", "Student Number", "Name", "Email", "Phone", "DOB", "Enrollment Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedStudent();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
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
    
    private void loadStudents() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            tableModel.setRowCount(0);
            
            for (Student student : students) {
                Object[] row = {
                    student.getStudentId(),
                    student.getStudentNumber(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getDateOfBirth(),
                    student.getEnrollmentDate(),
                    student.getStatus()
                };
                tableModel.addRow(row);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedStudentId = (int) tableModel.getValueAt(selectedRow, 0);
            
            try {
                Student student = studentDAO.getStudentById(selectedStudentId);
                if (student != null) {
                    txtStudentNumber.setText(student.getStudentNumber());
                    txtFirstName.setText(student.getFirstName());
                    txtLastName.setText(student.getLastName());
                    txtEmail.setText(student.getEmail());
                    txtPhone.setText(student.getPhone());
                    
                    if (student.getDateOfBirth() != null) {
                        txtDOB.setText(new SimpleDateFormat("yyyy-MM-dd")
                            .format(student.getDateOfBirth()));
                    }
                    
                    txtEnrollmentDate.setText(new SimpleDateFormat("yyyy-MM-dd")
                        .format(student.getEnrollmentDate()));
                    cmbStatus.setSelectedItem(student.getStatus());
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addStudent() {
        try {
            Student student = getStudentFromForm();
            
            if (studentDAO.addStudent(student)) {
                JOptionPane.showMessageDialog(this, 
                    "Student added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudents();
            }
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStudent() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a student to update", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Student student = getStudentFromForm();
            student.setStudentId(selectedStudentId);
            
            if (studentDAO.updateStudent(student)) {
                JOptionPane.showMessageDialog(this, 
                    "Student updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudents();
            }
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteStudent() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a student to delete", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this student?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (studentDAO.deleteStudent(selectedStudentId)) {
                    JOptionPane.showMessageDialog(this, 
                        "Student deleted successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudents();
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchStudents() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadStudents();
            return;
        }
        
        try {
            List<Student> students = studentDAO.searchStudents(searchTerm);
            tableModel.setRowCount(0);
            
            for (Student student : students) {
                Object[] row = {
                    student.getStudentId(),
                    student.getStudentNumber(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getDateOfBirth(),
                    student.getEnrollmentDate(),
                    student.getStatus()
                };
                tableModel.addRow(row);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Student getStudentFromForm() throws Exception {
        Student student = new Student();
        student.setStudentNumber(txtStudentNumber.getText().trim());
        student.setFirstName(txtFirstName.getText().trim());
        student.setLastName(txtLastName.getText().trim());
        student.setEmail(txtEmail.getText().trim());
        student.setPhone(txtPhone.getText().trim());
        student.setStatus((String) cmbStatus.getSelectedItem());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if (!txtDOB.getText().trim().isEmpty()) {
            student.setDateOfBirth(sdf.parse(txtDOB.getText().trim()));
        }
        
        student.setEnrollmentDate(sdf.parse(txtEnrollmentDate.getText().trim()));
        
        return student;
    }
    
    private void clearForm() {
        txtStudentNumber.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtDOB.setText("");
        txtEnrollmentDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        cmbStatus.setSelectedIndex(0);
        selectedStudentId = -1;
        studentTable.clearSelection();
    }
}