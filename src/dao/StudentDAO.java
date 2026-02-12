package dao;

import model.Student;
import util.DatabaseConnection;
import exception.DatabaseException;
import exception.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    
    private Connection connection;
    
    public StudentDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private void validateStudent(Student student) throws ValidationException {
        if (student.getStudentNumber() == null || student.getStudentNumber().trim().isEmpty()) {
            throw new ValidationException("Student number is required");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!student.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        if (student.getEnrollmentDate() == null) {
            throw new ValidationException("Enrollment date is required");
        }
    }

    public boolean addStudent(Student student) throws DatabaseException, ValidationException {
        validateStudent(student);
        
        String query = "INSERT INTO students (student_number, first_name, last_name, email, " +
                      "phone, date_of_birth, enrollment_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, student.getStudentNumber());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getPhone());
            stmt.setDate(6, student.getDateOfBirth() != null ? 
                         new java.sql.Date(student.getDateOfBirth().getTime()) : null);
            stmt.setDate(7, new java.sql.Date(student.getEnrollmentDate().getTime()));
            stmt.setString(8, student.getStatus() != null ? student.getStatus() : "Active");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Student number or email already exists");
        } catch (SQLException e) {
            throw new DatabaseException("Error adding student: " + e.getMessage(), e);
        }
    }

    public boolean updateStudent(Student student) throws DatabaseException, ValidationException {
        validateStudent(student);
        
        String query = "UPDATE students SET student_number=?, first_name=?, last_name=?, " +
                      "email=?, phone=?, date_of_birth=?, status=? WHERE student_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, student.getStudentNumber());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getPhone());
            stmt.setDate(6, student.getDateOfBirth() != null ? 
                         new java.sql.Date(student.getDateOfBirth().getTime()) : null);
            stmt.setString(7, student.getStatus());
            stmt.setInt(8, student.getStudentId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Student number or email already exists");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating student: " + e.getMessage(), e);
        }
    }

    public boolean deleteStudent(int studentId) throws DatabaseException {
        String query = "DELETE FROM students WHERE student_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting student: " + e.getMessage(), e);
        }
    }

    public Student getStudentById(int studentId) throws DatabaseException {
        String query = "SELECT * FROM students WHERE student_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving student: " + e.getMessage(), e);
        }
    }

    public List<Student> getAllStudents() throws DatabaseException {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students ORDER BY student_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving students: " + e.getMessage(), e);
        }
    }

    public List<Student> searchStudents(String searchTerm) throws DatabaseException {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students WHERE student_number LIKE ? OR " +
                      "first_name LIKE ? OR last_name LIKE ? ORDER BY student_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String search = "%" + searchTerm + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseException("Error searching students: " + e.getMessage(), e);
        }
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setStudentNumber(rs.getString("student_number"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setDateOfBirth(rs.getDate("date_of_birth"));
        student.setEnrollmentDate(rs.getDate("enrollment_date"));
        student.setStatus(rs.getString("status"));
        return student;
    }
}