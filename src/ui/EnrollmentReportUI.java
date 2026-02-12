package ui;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EnrollmentReportUI extends JFrame {
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCourses, lblFullCourses, lblLowEnrollment;
    private Connection connection;
    
    public EnrollmentReportUI() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
        
        setTitle("Course Enrollment Report - Decision Support");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        loadReport();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 126, 34));
        JLabel headerLabel = new JLabel("COURSE ENROLLMENT ANALYSIS REPORT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Key Metrics"));
        
        lblTotalCourses = createStatLabel("Total Courses: 0", new Color(52, 152, 219));
        lblFullCourses = createStatLabel("Full Courses: 0", new Color(231, 76, 60));
        lblLowEnrollment = createStatLabel("Low Enrollment (<50%): 0", new Color(241, 196, 15));
        
        statsPanel.add(lblTotalCourses);
        statsPanel.add(lblFullCourses);
        statsPanel.add(lblLowEnrollment);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Detailed Enrollment Report"));
        
        String[] columns = {
            "Course Code", "Course Name", "Credits", "Lecturer", "Department",
            "Enrolled Students", "Capacity", "Available Seats", "Enrollment %", "Status"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setAutoCreateRowSorter(true);

        reportTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        reportTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        reportTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Report Information"));
        JTextArea infoText = new JTextArea(4, 70);
        infoText.setEditable(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setText(
            "DECISION SUPPORT INFORMATION:\n\n" +
            "• FULL COURSES (Red): These courses have reached maximum capacity. " +
            "Consider: Adding more sections, increasing capacity, or finding alternative courses.\n\n" +
            "• HIGH ENROLLMENT (Green): Courses with 70-99% enrollment are performing well.\n\n" +
            "• MEDIUM ENROLLMENT (Yellow): Courses with 50-69% enrollment. Monitor for trends.\n\n" +
            "• LOW ENROLLMENT (Orange): Courses below 50% capacity. " +
            "Consider: Marketing efforts, schedule changes, or cancellation if critically low."
        );
        infoPanel.add(new JScrollPane(infoText));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefresh = createButton("🔄 Refresh Report", new Color(46, 204, 113));
        JButton btnExport = createButton("💾 Export to CSV", new Color(52, 152, 219));
        JButton btnPrint = createButton("🖨 Print Report", new Color(149, 165, 166));
        
        btnRefresh.addActionListener(e -> loadReport());
        btnExport.addActionListener(e -> exportReport());
        btnPrint.addActionListener(e -> printReport());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnPrint);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(statsPanel, BorderLayout.NORTH);
        topPanel.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(color);
        label.setBorder(BorderFactory.createLineBorder(color, 2));
        label.setOpaque(true);
        label.setBackground(new Color(236, 240, 241));
        label.setPreferredSize(new Dimension(250, 60));
        return label;
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
    
    private void loadReport() {
        try {

            tableModel.setRowCount(0);

            String query = "SELECT * FROM course_enrollment_summary ORDER BY course_code";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            int totalCourses = 0;
            int fullCourses = 0;
            int lowEnrollment = 0;
            
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                int credits = rs.getInt("credits");
                String lecturerName = rs.getString("lecturer_name");
                String department = rs.getString("department");
                int totalStudents = rs.getInt("total_students");
                int capacity = rs.getInt("capacity");
                int availableSeats = rs.getInt("available_seats");

                double enrollmentPercentage = capacity > 0 ? 
                    (totalStudents * 100.0 / capacity) : 0;

                String status;
                if (availableSeats <= 0) {
                    status = "FULL - Add Section";
                    fullCourses++;
                } else if (enrollmentPercentage >= 70) {
                    status = "High Enrollment";
                } else if (enrollmentPercentage >= 50) {
                    status = "Medium Enrollment";
                } else if (enrollmentPercentage >= 30) {
                    status = "Low - Monitor";
                    lowEnrollment++;
                } else {
                    status = "Critical - Review";
                    lowEnrollment++;
                }
                
                Object[] row = {
                    courseCode,
                    courseName,
                    credits,
                    lecturerName != null ? lecturerName : "Not Assigned",
                    department != null ? department : "N/A",
                    totalStudents,
                    capacity,
                    availableSeats,
                    String.format("%.1f%%", enrollmentPercentage),
                    status
                };
                
                tableModel.addRow(row);
                totalCourses++;
            }

            lblTotalCourses.setText("Total Courses: " + totalCourses);
            lblFullCourses.setText("Full Courses: " + fullCourses);
            lblLowEnrollment.setText("Low Enrollment (<50%): " + lowEnrollment);

            reportTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                    
                    if (!isSelected) {
                        String status = (String) table.getValueAt(row, 9);
                        if (status.contains("FULL")) {
                            c.setBackground(new Color(255, 200, 200));
                        } else if (status.contains("High")) {
                            c.setBackground(new Color(200, 255, 200));
                        } else if (status.contains("Medium")) {
                            c.setBackground(new Color(255, 255, 200));
                        } else if (status.contains("Low") || status.contains("Critical")) {
                            c.setBackground(new Color(255, 230, 200));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    }
                    return c;
                }
            });
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading report: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report as CSV");
        fileChooser.setSelectedFile(new java.io.File("enrollment_report.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                java.io.FileWriter fw = new java.io.FileWriter(fileToSave);

                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    fw.write(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
                
                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        fw.write(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }
                
                fw.close();
                JOptionPane.showMessageDialog(this, 
                    "Report exported successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting report: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void printReport() {
        try {
            reportTable.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error printing report: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}