package ui;

import util.DatabaseConnection;
import util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dashboard extends JFrame {
    
    private JLabel lblTotalStudents, lblTotalCourses, lblTotalEnrollments;
    private JLabel lblWelcome;
    private JPanel statsPanel;
    
    public Dashboard() {

        if (!SessionManager.getInstance().isLoggedIn()) {
            JOptionPane.showMessageDialog(null, 
                "Please login first", 
                "Access Denied", JOptionPane.WARNING_MESSAGE);
            new LoginUI().setVisible(true);
            dispose();
            return;
        }
        
        setTitle("SAMS - Dashboard (" + SessionManager.getInstance().getCurrentUserRole() + ")");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadStatistics();
    }
    
    private void initComponents() {

        JMenuBar menuBar = new JMenuBar();

        JMenu studentMenu = new JMenu("Students");
        JMenuItem manageStudents = new JMenuItem("Manage Students");
        manageStudents.addActionListener(e -> openStudentManagement());
        studentMenu.add(manageStudents);

        JMenu lecturerMenu = new JMenu("Lecturers");
        JMenuItem manageLecturers = new JMenuItem("Manage Lecturers");
        manageLecturers.addActionListener(e -> openLecturerManagement());
        lecturerMenu.add(manageLecturers);

        JMenu courseMenu = new JMenu("Courses");
        JMenuItem manageCourses = new JMenuItem("Manage Courses");
        manageCourses.addActionListener(e -> openCourseManagement());
        courseMenu.add(manageCourses);

        JMenu enrollmentMenu = new JMenu("Enrollments");
        JMenuItem manageEnrollments = new JMenuItem("Course Enrollment");
        manageEnrollments.addActionListener(e -> openEnrollmentManagement());
        enrollmentMenu.add(manageEnrollments);

        JMenu academicMenu = new JMenu("Academic");
        JMenuItem attendance = new JMenuItem("Attendance Tracking");
        attendance.addActionListener(e -> openAttendanceTracking());
        JMenuItem assessments = new JMenuItem("Assessment Results");
        assessments.addActionListener(e -> openAssessmentResults());
        academicMenu.add(attendance);
        academicMenu.add(assessments);

        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem studentReport = new JMenuItem("Student Performance Report");
        studentReport.addActionListener(e -> openStudentReport());
        JMenuItem enrollmentReport = new JMenuItem("Course Enrollment Report");
        enrollmentReport.addActionListener(e -> openEnrollmentReport());
        reportsMenu.add(studentReport);
        reportsMenu.add(enrollmentReport);

        if (SessionManager.getInstance().isAdmin()) {
            JMenu userMenu = new JMenu("Users");
            JMenuItem manageUsers = new JMenuItem("Manage Users");
            manageUsers.addActionListener(e -> openUserManagement());
            userMenu.add(manageUsers);
            menuBar.add(userMenu);
        }

        JMenu accountMenu = new JMenu("Account");
        JMenuItem profile = new JMenuItem("My Profile");
        profile.addActionListener(e -> showProfile());
        JMenuItem changePassword = new JMenuItem("Change Password");
        changePassword.addActionListener(e -> openChangePassword());
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> performLogout());
        accountMenu.add(profile);
        accountMenu.add(changePassword);
        accountMenu.addSeparator();
        accountMenu.add(logout);

        JMenu exitMenu = new JMenu("Exit");
        JMenuItem exitItem = new JMenuItem("Exit System");
        exitItem.addActionListener(e -> exitApplication());
        exitMenu.add(exitItem);
        
        menuBar.add(studentMenu);
        menuBar.add(lecturerMenu);
        menuBar.add(courseMenu);
        menuBar.add(enrollmentMenu);
        menuBar.add(academicMenu);
        menuBar.add(reportsMenu);
        menuBar.add(accountMenu);
        menuBar.add(exitMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel headerLabel = new JLabel("SMART ACADEMIC MANAGEMENT SYSTEM");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);

        lblWelcome = new JLabel("Welcome, " + SessionManager.getInstance().getCurrentUserFullName() + 
                                " (" + SessionManager.getInstance().getCurrentUserRole() + ")");
        lblWelcome.setFont(new Font("Arial", Font.ITALIC, 14));
        lblWelcome.setForeground(new Color(236, 240, 241));
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(lblWelcome, BorderLayout.EAST);

        statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("System Statistics"));
        
        lblTotalStudents = createStatLabel("Total Students: 0", new Color(46, 204, 113));
        lblTotalCourses = createStatLabel("Total Courses: 0", new Color(52, 152, 219));
        lblTotalEnrollments = createStatLabel("Active Enrollments: 0", new Color(155, 89, 182));
        
        statsPanel.add(lblTotalStudents);
        statsPanel.add(lblTotalCourses);
        statsPanel.add(lblTotalEnrollments);

        JPanel quickAccessPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        quickAccessPanel.setBorder(BorderFactory.createTitledBorder("Quick Access"));
        
        JButton btnManageStudents = createQuickAccessButton("👨‍🎓 Manage Students", 
            new Color(52, 152, 219));
        btnManageStudents.addActionListener(e -> openStudentManagement());
        
        JButton btnManageLecturers = createQuickAccessButton("👨‍🏫 Manage Lecturers", 
            new Color(22, 160, 133));
        btnManageLecturers.addActionListener(e -> openLecturerManagement());
        
        JButton btnManageCourses = createQuickAccessButton("📚 Manage Courses", 
            new Color(41, 128, 185));
        btnManageCourses.addActionListener(e -> openCourseManagement());
        
        JButton btnEnrollment = createQuickAccessButton("📝 Course Enrollment", 
            new Color(46, 204, 113));
        btnEnrollment.addActionListener(e -> openEnrollmentManagement());
        
        JButton btnAttendance = createQuickAccessButton("📋 Attendance Tracking", 
            new Color(142, 68, 173));
        btnAttendance.addActionListener(e -> openAttendanceTracking());
        
        JButton btnAssessments = createQuickAccessButton("✍ Assessment Results", 
            new Color(211, 84, 0));
        btnAssessments.addActionListener(e -> openAssessmentResults());
        
        JButton btnReports = createQuickAccessButton("📊 View Reports", 
            new Color(230, 126, 34));
        btnReports.addActionListener(e -> openEnrollmentReport());
        
        JButton btnUsers = createQuickAccessButton("👥 User Management", 
            new Color(155, 89, 182));
        if (SessionManager.getInstance().isAdmin()) {
            btnUsers.addActionListener(e -> openUserManagement());
        } else {
            btnUsers.setEnabled(false);
        }
        
        JButton btnExit = createQuickAccessButton("🚪 Exit System", 
            new Color(231, 76, 60));
        btnExit.addActionListener(e -> exitApplication());
        
        quickAccessPanel.add(btnManageStudents);
        quickAccessPanel.add(btnManageLecturers);
        quickAccessPanel.add(btnManageCourses);
        quickAccessPanel.add(btnEnrollment);
        quickAccessPanel.add(btnAttendance);
        quickAccessPanel.add(btnAssessments);
        quickAccessPanel.add(btnReports);
        quickAccessPanel.add(btnUsers);
        quickAccessPanel.add(btnExit);
        
       
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(quickAccessPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        
        add(mainPanel);
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(color);
        label.setBorder(BorderFactory.createLineBorder(color, 2));
        label.setOpaque(true);
        label.setBackground(new Color(236, 240, 241));
        label.setPreferredSize(new Dimension(250, 80));
        return label;
    }
    
    private JButton createQuickAccessButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color buttonColor = color;
                if (getModel().isPressed()) {
                    buttonColor = color.darker().darker();
                } else if (getModel().isRollover()) {
                    buttonColor = color.darker();
                }
                
                g2d.setColor(buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawRoundRect(0, 1, getWidth()-1, getHeight()-1, 15, 15);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 100));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadStatistics() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) FROM students WHERE status='Active'");
            if (rs1.next()) {
                lblTotalStudents.setText("Total Students: " + rs1.getInt(1));
            }

            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM courses");
            if (rs2.next()) {
                lblTotalCourses.setText("Total Courses: " + rs2.getInt(1));
            }

            Statement stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery("SELECT COUNT(*) FROM enrollments WHERE status='Enrolled'");
            if (rs3.next()) {
                lblTotalEnrollments.setText("Active Enrollments: " + rs3.getInt(1));
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading statistics: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openStudentManagement() {
        new StudentManagementUI().setVisible(true);
    }
    
    private void openLecturerManagement() {
        new LecturerManagementUI().setVisible(true);
    }
    
    private void openCourseManagement() {
        new CourseManagementUI().setVisible(true);
    }
    
    private void openEnrollmentManagement() {
        new EnrollmentUI().setVisible(true);
    }
    
    private void openAttendanceTracking() {
        new AttendanceTrackingUI().setVisible(true);
    }
    
    private void openAssessmentResults() {
        new AssessmentResultsUI().setVisible(true);
    }
    
    private void openStudentReport() {
        JOptionPane.showMessageDialog(this, 
            "Student Report functionality - Opens JasperReports viewer",
            "Reports", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openEnrollmentReport() {
        try {
            new EnrollmentReportUI().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error opening report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openUserManagement() {
        new UserManagementUI().setVisible(true);
    }
    
    private void showProfile() {
        SessionManager session = SessionManager.getInstance();
        String message = "Username: " + session.getCurrentUsername() + "\n" +
                        "Full Name: " + session.getCurrentUserFullName() + "\n" +
                        "Role: " + session.getCurrentUserRole() + "\n" +
                        "Session Duration: " + session.getSessionDuration() + " minutes";
        
        JOptionPane.showMessageDialog(this, message, 
            "My Profile", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openChangePassword() {
        new ChangePasswordUI(this).setVisible(true);
    }
    
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            new LoginUI().setVisible(true);
        }
    }
    
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit SAMS?",
            "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            try {
                DatabaseConnection.getInstance().closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}