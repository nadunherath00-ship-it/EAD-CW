package dao;

import model.Enrollment;
import util.DatabaseConnection;
import exception.DatabaseException;
import exception.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EnrollmentDAO {
    
    private Connection connection;
    
    public EnrollmentDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    private void validateEnrollment(Enrollment enrollment) throws ValidationException {
        if (enrollment.getStudentId() <= 0) {
            throw new ValidationException("Valid student must be selected");
        }
        if (enrollment.getCourseId() <= 0) {
            throw new ValidationException("Valid course must be selected");
        }
        if (enrollment.getEnrollmentDate() == null) {
            throw new ValidationException("Enrollment date is required");
        }
    }

    private boolean isAlreadyEnrolled(int studentId, int courseId) throws SQLException {
        String query = "SELECT COUNT(*) FROM enrollments WHERE student_id=? AND course_id=? " +
                      "AND status='Enrolled'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean addEnrollment(Enrollment enrollment) throws DatabaseException, ValidationException {
        validateEnrollment(enrollment);
        
        try {
            if (isAlreadyEnrolled(enrollment.getStudentId(), enrollment.getCourseId())) {
                throw new ValidationException("Student is already enrolled in this course");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking enrollment status", e);
        }
        
        String query = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setDate(3, new java.sql.Date(enrollment.getEnrollmentDate().getTime()));
            stmt.setString(4, enrollment.getStatus() != null ? enrollment.getStatus() : "Enrolled");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adding enrollment: " + e.getMessage(), e);
        }
    }

    public boolean updateEnrollment(Enrollment enrollment) throws DatabaseException {
        String query = "UPDATE enrollments SET status=?, grade=? WHERE enrollment_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, enrollment.getStatus());
            stmt.setString(2, enrollment.getGrade());
            stmt.setInt(3, enrollment.getEnrollmentId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating enrollment: " + e.getMessage(), e);
        }
    }

    public boolean deleteEnrollment(int enrollmentId) throws DatabaseException {
        String query = "DELETE FROM enrollments WHERE enrollment_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, enrollmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting enrollment: " + e.getMessage(), e);
        }
    }
    
    public List<Enrollment> getAllEnrollments() throws DatabaseException {
        List<Enrollment> enrollments = new ArrayList<>();
        String query = "SELECT e.*, s.student_number, CONCAT(s.first_name, ' ', s.last_name) as student_name, " +
                      "c.course_code, c.course_name FROM enrollments e " +
                      "JOIN students s ON e.student_id = s.student_id " +
                      "JOIN courses c ON e.course_id = c.course_id " +
                      "ORDER BY e.enrollment_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                enrollments.add(extractEnrollmentFromResultSet(rs));
            }
            return enrollments;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving enrollments: " + e.getMessage(), e);
        }
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) throws DatabaseException {
        List<Enrollment> enrollments = new ArrayList<>();
        String query = "SELECT e.*, c.course_code, c.course_name FROM enrollments e " +
                      "JOIN courses c ON e.course_id = c.course_id " +
                      "WHERE e.student_id = ? ORDER BY e.enrollment_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setCourseId(rs.getInt("course_id"));
                enrollment.setEnrollmentDate(rs.getDate("enrollment_date"));
                enrollment.setStatus(rs.getString("status"));
                enrollment.setGrade(rs.getString("grade"));
                enrollment.setCourseCode(rs.getString("course_code"));
                enrollment.setCourseName(rs.getString("course_name"));
                enrollments.add(enrollment);
            }
            return enrollments;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving student enrollments: " + e.getMessage(), e);
        }
    }

    public List<Enrollment> getEnrollmentsByCourse(int courseId) throws DatabaseException {
        List<Enrollment> enrollments = new ArrayList<>();
        String query = "SELECT e.*, s.student_number, CONCAT(s.first_name, ' ', s.last_name) as student_name " +
                      "FROM enrollments e JOIN students s ON e.student_id = s.student_id " +
                      "WHERE e.course_id = ? AND e.status = 'Enrolled' " +
                      "ORDER BY s.student_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setCourseId(rs.getInt("course_id"));
                enrollment.setEnrollmentDate(rs.getDate("enrollment_date"));
                enrollment.setStatus(rs.getString("status"));
                enrollment.setGrade(rs.getString("grade"));
                enrollment.setStudentName(rs.getString("student_name"));
                enrollments.add(enrollment);
            }
            return enrollments;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving course enrollments: " + e.getMessage(), e);
        }
    }

    private Enrollment extractEnrollmentFromResultSet(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setCourseId(rs.getInt("course_id"));
        enrollment.setEnrollmentDate(rs.getDate("enrollment_date"));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setGrade(rs.getString("grade"));
        enrollment.setStudentName(rs.getString("student_name"));
        enrollment.setCourseCode(rs.getString("course_code"));
        enrollment.setCourseName(rs.getString("course_name"));
        return enrollment;
    }
}