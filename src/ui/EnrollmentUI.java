package ui;

import dao.EnrollmentDAO;
import dao.StudentDAO;
import model.Enrollment;
import model.Student;
import exception.DatabaseException;
import exception.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EnrollmentUI extends JFrame {
    
    private JComboBox<Student> cmbStudent;
    private JComboBox<CourseItem> cmbCourse;
    private JFormattedTextField txtEnrollmentDate;
    private JTable enrollmentTable;
    private DefaultTableModel tableModel;
    private JButton btnEnroll, btnWithdraw, btnRefresh;
    
    private EnrollmentDAO enrollmentDAO;
    private StudentDAO studentDAO;
    private Connection connection;
    
    public EnrollmentUI() {
        try {
            enrollmentDAO = new EnrollmentDAO();
            studentDAO = new StudentDAO();
            connection = util.DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Course Enrollment");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadStudents();
        loadCourses();
        loadEnrollments();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(46, 204, 113));
        JLabel headerLabel = new JLabel("COURSE ENROLLMENT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Enrollment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Student:"), gbc);
        gbc.gridx = 1;
        cmbStudent = new JComboBox<>();
        cmbStudent.setPreferredSize(new Dimension(300, 25));
        formPanel.add(cmbStudent, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Select Course:"), gbc);
        gbc.gridx = 1;
        cmbCourse = new JComboBox<>();
        cmbCourse.setPreferredSize(new Dimension(300, 25));
        formPanel.add(cmbCourse, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Enrollment Date:"), gbc);
        gbc.gridx = 1;
        txtEnrollmentDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtEnrollmentDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtEnrollmentDate.setPreferredSize(new Dimension(300, 25));
        formPanel.add(txtEnrollmentDate, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        btnEnroll = createButton("✓ Enroll Student", new Color(46, 204, 113));
        btnWithdraw = createButton("✗ Withdraw Enrollment", new Color(231, 76, 60));
        btnRefresh = createButton("🔄 Refresh List", new Color(52, 152, 219));
        
        btnEnroll.addActionListener(e -> enrollStudent());
        btnWithdraw.addActionListener(e -> withdrawEnrollment());
        btnRefresh.addActionListener(e -> loadEnrollments());
        
        buttonPanel.add(btnEnroll);
        buttonPanel.add(btnWithdraw);
        buttonPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Current Enrollments"));
        
        String[] columns = {"Enrollment ID", "Student Number", "Student Name", 
                           "Course Code", "Course Name", "Enrollment Date", "Status", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        enrollmentTable = new JTable(tableModel);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
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
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadStudents() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            cmbStudent.removeAllItems();
            
            for (Student student : students) {
                if ("Active".equals(student.getStatus())) {
                    cmbStudent.addItem(student);
                }
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCourses() {
        try {
            cmbCourse.removeAllItems();
            String query = "SELECT c.course_id, c.course_code, c.course_name, c.credits, " +
                          "CONCAT(l.first_name, ' ', l.last_name) as lecturer_name " +
                          "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.lecturer_id " +
                          "ORDER BY c.course_code";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                CourseItem course = new CourseItem(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credits"),
                    rs.getString("lecturer_name")
                );
                cmbCourse.addItem(course);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading courses: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadEnrollments() {
        try {
            List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();
            tableModel.setRowCount(0);
            
            for (Enrollment enrollment : enrollments) {
                Object[] row = {
                    enrollment.getEnrollmentId(),
                    "", // Student number - would need to join
                    enrollment.getStudentName(),
                    enrollment.getCourseCode(),
                    enrollment.getCourseName(),
                    enrollment.getEnrollmentDate(),
                    enrollment.getStatus(),
                    enrollment.getGrade() != null ? enrollment.getGrade() : "N/A"
                };
                tableModel.addRow(row);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void enrollStudent() {
        try {
            Student selectedStudent = (Student) cmbStudent.getSelectedItem();
            CourseItem selectedCourse = (CourseItem) cmbCourse.getSelectedItem();
            
            if (selectedStudent == null || selectedCourse == null) {
                JOptionPane.showMessageDialog(this, 
                    "Please select both student and course", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(selectedStudent.getStudentId());
            enrollment.setCourseId(selectedCourse.courseId);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            enrollment.setEnrollmentDate(sdf.parse(txtEnrollmentDate.getText()));
            enrollment.setStatus("Enrolled");
            
            if (enrollmentDAO.addEnrollment(enrollment)) {
                JOptionPane.showMessageDialog(this, 
                    "Student enrolled successfully!\n\n" +
                    "Student: " + selectedStudent.getFullName() + "\n" +
                    "Course: " + selectedCourse.toString(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEnrollments();
            }
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void withdrawEnrollment() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an enrollment to withdraw", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int enrollmentId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 2);
        String courseName = (String) tableModel.getValueAt(selectedRow, 4);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to withdraw this enrollment?\n\n" +
            "Student: " + studentName + "\n" +
            "Course: " + courseName,
            "Confirm Withdrawal", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (enrollmentDAO.deleteEnrollment(enrollmentId)) {
                    JOptionPane.showMessageDialog(this, 
                        "Enrollment withdrawn successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEnrollments();
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class CourseItem {
        int courseId;
        String courseCode;
        String courseName;
        int credits;
        String lecturerName;
        
        CourseItem(int id, String code, String name, int credits, String lecturer) {
            this.courseId = id;
            this.courseCode = code;
            this.courseName = name;
            this.credits = credits;
            this.lecturerName = lecturer;
        }
        
        @Override
        public String toString() {
            return courseCode + " - " + courseName + " (" + credits + " credits)";
        }
    }
}