package ui;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceTrackingUI extends JFrame {
    
    private JComboBox<CourseItem> cmbCourse;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JButton btnMarkPresent, btnMarkAbsent, btnMarkLate, btnSave, btnRefresh;
    private JLabel lblDate, lblCourseInfo;
    
    private Connection connection;
    
    public AttendanceTrackingUI() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Attendance Tracking");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadCourses();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(142, 68, 173));
        JLabel headerLabel = new JLabel("📋 ATTENDANCE TRACKING");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Attendance Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Select Course:"), gbc);
        gbc.gridx = 1;
        cmbCourse = new JComboBox<>();
        cmbCourse.setPreferredSize(new Dimension(300, 30));
        cmbCourse.addActionListener(e -> loadStudentsForCourse());
        controlPanel.add(cmbCourse, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 3;
        lblDate = new JLabel(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        lblDate.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(lblDate, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        lblCourseInfo = new JLabel(" ");
        lblCourseInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblCourseInfo.setForeground(new Color(52, 73, 94));
        controlPanel.add(lblCourseInfo, gbc);
        gbc.gridwidth = 1;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnMarkPresent = createButton("✓ Mark Present", new Color(46, 204, 113));
        btnMarkAbsent = createButton("✗ Mark Absent", new Color(231, 76, 60));
        btnMarkLate = createButton("⏰ Mark Late", new Color(241, 196, 15));
        btnSave = createButton("💾 Save Attendance", new Color(52, 152, 219));
        btnRefresh = createButton("🔄 Refresh", new Color(149, 165, 166));
        
        btnMarkPresent.addActionListener(e -> markAttendance("Present"));
        btnMarkAbsent.addActionListener(e -> markAttendance("Absent"));
        btnMarkLate.addActionListener(e -> markAttendance("Late"));
        btnSave.addActionListener(e -> saveAttendance());
        btnRefresh.addActionListener(e -> loadStudentsForCourse());
        
        buttonPanel.add(btnMarkPresent);
        buttonPanel.add(btnMarkAbsent);
        buttonPanel.add(btnMarkLate);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Student List"));
        
        String[] columns = {"Select", "Student Number", "Student Name", "Status", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3 || column == 4;
            }
        };
        
        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(30);
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        attendanceTable.getColumnModel().getColumn(3).setCellEditor(
            new DefaultCellEditor(new JComboBox<>(new String[]{"Present", "Absent", "Late"})));
        
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
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
            String query = "SELECT c.course_id, c.course_code, c.course_name, " +
                          "CONCAT(l.first_name, ' ', l.last_name) as lecturer " +
                          "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.lecturer_id " +
                          "ORDER BY c.course_code";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                cmbCourse.addItem(new CourseItem(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("lecturer")
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
        
        lblCourseInfo.setText("Lecturer: " + (course.lecturer != null ? course.lecturer : "Not Assigned"));
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT s.student_number, CONCAT(s.first_name, ' ', s.last_name) as name, " +
                          "e.enrollment_id " +
                          "FROM students s " +
                          "JOIN enrollments e ON s.student_id = e.student_id " +
                          "WHERE e.course_id = ? AND e.status = 'Enrolled' " +
                          "ORDER BY s.student_number";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, course.id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    false,
                    rs.getString("student_number"),
                    rs.getString("name"),
                    "Present",
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
    
    private void markAttendance(String status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) tableModel.getValueAt(i, 0);
            if (selected != null && selected) {
                tableModel.setValueAt(status, i, 3);
            }
        }
    }
    
    private void saveAttendance() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String dateStr = lblDate.getText();
            int savedCount = 0;
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String studentNumber = (String) tableModel.getValueAt(i, 1);
                String status = (String) tableModel.getValueAt(i, 3);
                String remarks = (String) tableModel.getValueAt(i, 4);

                String query1 = "SELECT e.enrollment_id FROM enrollments e " +
                               "JOIN students s ON e.student_id = s.student_id " +
                               "WHERE s.student_number = ? AND e.course_id = ?";
                PreparedStatement stmt1 = connection.prepareStatement(query1);
                stmt1.setString(1, studentNumber);
                stmt1.setInt(2, course.id);
                ResultSet rs = stmt1.executeQuery();
                
                if (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");

                    String query2 = "INSERT INTO attendance (enrollment_id, attendance_date, status, remarks) " +
                                   "VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt2 = connection.prepareStatement(query2);
                    stmt2.setInt(1, enrollmentId);
                    stmt2.setDate(2, java.sql.Date.valueOf(dateStr));
                    stmt2.setString(3, status);
                    stmt2.setString(4, remarks);
                    
                    if (stmt2.executeUpdate() > 0) {
                        savedCount++;
                    }
                }
            }
            
            JOptionPane.showMessageDialog(this, 
                "Attendance saved for " + savedCount + " students!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving attendance: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    class CourseItem {
        int id;
        String code, name, lecturer;
        
        CourseItem(int id, String code, String name, String lecturer) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.lecturer = lecturer;
        }
        
        @Override
        public String toString() {
            return code + " - " + name;
        }
    }
}