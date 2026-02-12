package ui;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AssessmentResultsUI extends JFrame {
    
    private JComboBox<CourseItem> cmbCourse;
    private JComboBox<String> cmbAssessmentType;
    private JTextField txtTotalMarks;
    private JTable assessmentTable;
    private DefaultTableModel tableModel;
    private JButton btnSave, btnClear, btnRefresh, btnCalculateGrades;
    
    private Connection connection;
    
    public AssessmentResultsUI() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Assessment Results");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadCourses();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(211, 84, 0));
        JLabel headerLabel = new JLabel("📝 ASSESSMENT RESULTS");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Assessment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Select Course:"), gbc);
        gbc.gridx = 1;
        cmbCourse = new JComboBox<>();
        cmbCourse.setPreferredSize(new Dimension(250, 30));
        cmbCourse.addActionListener(e -> loadStudentsForCourse());
        controlPanel.add(cmbCourse, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(new JLabel("Assessment Type:"), gbc);
        gbc.gridx = 3;
        cmbAssessmentType = new JComboBox<>(new String[]{"Quiz", "Assignment", "Midterm", "Final"});
        cmbAssessmentType.setPreferredSize(new Dimension(150, 30));
        controlPanel.add(cmbAssessmentType, gbc);
        
        gbc.gridx = 4;
        controlPanel.add(new JLabel("Total Marks:"), gbc);
        gbc.gridx = 5;
        txtTotalMarks = new JTextField(10);
        txtTotalMarks.setText("100");
        controlPanel.add(txtTotalMarks, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnSave = createButton("💾 Save Results", new Color(46, 204, 113));
        btnCalculateGrades = createButton("🎓 Calculate Grades", new Color(52, 152, 219));
        btnClear = createButton("🔄 Clear", new Color(149, 165, 166));
        btnRefresh = createButton("🔄 Refresh", new Color(26, 188, 156));
        
        btnSave.addActionListener(e -> saveAssessments());
        btnCalculateGrades.addActionListener(e -> calculateGrades());
        btnClear.addActionListener(e -> clearTable());
        btnRefresh.addActionListener(e -> loadStudentsForCourse());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCalculateGrades);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Student Assessment Marks"));
        
        String[] columns = {"Student Number", "Student Name", "Marks Obtained", "Percentage", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; 
            }
        };
        
        assessmentTable = new JTable(tableModel);
        assessmentTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(assessmentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Grading Scale"));
        JLabel lblGrading = new JLabel("<html><b>A:</b> 80-100% | <b>B:</b> 70-79% | " +
            "<b>C:</b> 60-69% | <b>D:</b> 50-59% | <b>F:</b> Below 50%</html>");
        infoPanel.add(lblGrading);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
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
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadCourses() {
        try {
            cmbCourse.removeAllItems();
            String query = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                cmbCourse.addItem(new CourseItem(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading courses: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadStudentsForCourse() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course == null) return;
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT s.student_number, CONCAT(s.first_name, ' ', s.last_name) as name " +
                          "FROM students s " +
                          "JOIN enrollments e ON s.student_id = e.student_id " +
                          "WHERE e.course_id = ? AND e.status = 'Enrolled' " +
                          "ORDER BY s.student_number";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, course.id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("student_number"),
                    rs.getString("name"),
                    "",
                    "",
                    ""
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading students: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calculateGrades() {
        try {
            double totalMarks = Double.parseDouble(txtTotalMarks.getText().trim());
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String marksStr = (String) tableModel.getValueAt(i, 2);
                if (marksStr != null && !marksStr.trim().isEmpty()) {
                    double marks = Double.parseDouble(marksStr);
                    double percentage = (marks / totalMarks) * 100;
                    String grade = getGrade(percentage);
                    
                    tableModel.setValueAt(String.format("%.2f%%", percentage), i, 3);
                    tableModel.setValueAt(grade, i, 4);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for marks",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getGrade(double percentage) {
        if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B";
        else if (percentage >= 60) return "C";
        else if (percentage >= 50) return "D";
        else return "F";
    }
    
    private void saveAssessments() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String assessmentType = (String) cmbAssessmentType.getSelectedItem();
            double totalMarks = Double.parseDouble(txtTotalMarks.getText().trim());
            int savedCount = 0;
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String studentNumber = (String) tableModel.getValueAt(i, 0);
                String marksStr = (String) tableModel.getValueAt(i, 2);
                
                if (marksStr != null && !marksStr.trim().isEmpty()) {
                    double marks = Double.parseDouble(marksStr);

                    String query1 = "SELECT e.enrollment_id FROM enrollments e " +
                                   "JOIN students s ON e.student_id = s.student_id " +
                                   "WHERE s.student_number = ? AND e.course_id = ?";
                    PreparedStatement stmt1 = connection.prepareStatement(query1);
                    stmt1.setString(1, studentNumber);
                    stmt1.setInt(2, course.id);
                    ResultSet rs = stmt1.executeQuery();
                    
                    if (rs.next()) {
                        int enrollmentId = rs.getInt("enrollment_id");

                        String query2 = "INSERT INTO assessments (enrollment_id, assessment_type, " +
                                       "marks_obtained, total_marks, assessment_date) " +
                                       "VALUES (?, ?, ?, ?, CURDATE())";
                        PreparedStatement stmt2 = connection.prepareStatement(query2);
                        stmt2.setInt(1, enrollmentId);
                        stmt2.setString(2, assessmentType);
                        stmt2.setDouble(3, marks);
                        stmt2.setDouble(4, totalMarks);
                        
                        if (stmt2.executeUpdate() > 0) {
                            savedCount++;
                        }
                    }
                }
            }
            
            JOptionPane.showMessageDialog(this, 
                "Assessment results saved for " + savedCount + " students!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving assessments: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearTable() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, 2);
            tableModel.setValueAt("", i, 3);
            tableModel.setValueAt("", i, 4);
        }
    }
    
    class CourseItem {
        int id;
        String code, name;
        
        CourseItem(int id, String code, String name) {
            this.id = id;
            this.name = name;
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code + " - " + name;
        }
    }
}