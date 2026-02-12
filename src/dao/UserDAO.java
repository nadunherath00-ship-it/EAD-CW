package dao;

import model.User;
import util.DatabaseConnection;
import exception.DatabaseException;
import exception.ValidationException;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    private Connection connection;
    
    public UserDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private String hashPassword(String password) throws DatabaseException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new DatabaseException("Error hashing password", e);
        }
    }

    private void validateUser(User user) throws ValidationException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (user.getUsername().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (user.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new ValidationException("Role is required");
        }
    }

    public User authenticate(String username, String password) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username=? AND status='Active'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = hashPassword(password);
                
                if (storedHash.equals(inputHash)) {
                    updateLastLogin(rs.getInt("user_id"));
                    
                    User user = extractUserFromResultSet(rs);
                    user.setPassword(null); 
                    return user;
                }
            }
            return null; 
        } catch (SQLException e) {
            throw new DatabaseException("Error authenticating user: " + e.getMessage(), e);
        }
    }

    private void updateLastLogin(int userId) throws SQLException {
        String query = "UPDATE users SET last_login=NOW() WHERE user_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public boolean addUser(User user) throws DatabaseException, ValidationException {
        validateUser(user);
        
        String query = "INSERT INTO users (username, password, full_name, email, role, status) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashPassword(user.getPassword()));
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getStatus() != null ? user.getStatus() : "Active");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Username or email already exists");
        } catch (SQLException e) {
            throw new DatabaseException("Error adding user: " + e.getMessage(), e);
        }
    }

    public boolean updateUser(User user) throws DatabaseException, ValidationException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Valid email is required");
        }
        
        String query = "UPDATE users SET full_name=?, email=?, role=?, status=? WHERE user_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getStatus());
            stmt.setInt(5, user.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Email already exists");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user: " + e.getMessage(), e);
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) 
            throws DatabaseException, ValidationException {
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }

        String query = "SELECT password FROM users WHERE user_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                String oldHash = hashPassword(oldPassword);
                
                if (!storedHash.equals(oldHash)) {
                    throw new ValidationException("Current password is incorrect");
                }
            } else {
                throw new DatabaseException("User not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error verifying password: " + e.getMessage(), e);
        }

        String updateQuery = "UPDATE users SET password=? WHERE user_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password: " + e.getMessage(), e);
        }
    }

    public boolean resetPassword(int userId, String newPassword) 
            throws DatabaseException, ValidationException {
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        
        String query = "UPDATE users SET password=? WHERE user_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error resetting password: " + e.getMessage(), e);
        }
    }

    public boolean deleteUser(int userId) throws DatabaseException {
        String query = "DELETE FROM users WHERE user_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user: " + e.getMessage(), e);
        }
    }

    public User getUserById(int userId) throws DatabaseException {
        String query = "SELECT * FROM users WHERE user_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setPassword(null); 
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving user: " + e.getMessage(), e);
        }
    }

    public User getUserByUsername(String username) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setPassword(null);
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving user: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() throws DatabaseException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setPassword(null);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving users: " + e.getMessage(), e);
        }
    }

    public List<User> searchUsers(String searchTerm) throws DatabaseException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users WHERE username LIKE ? OR full_name LIKE ? " +
                      "ORDER BY username";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String search = "%" + searchTerm + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setPassword(null); 
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error searching users: " + e.getMessage(), e);
        }
    }

    public boolean usernameExists(String username) throws DatabaseException {
        String query = "SELECT COUNT(*) FROM users WHERE username=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error checking username: " + e.getMessage(), e);
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        return user;
    }
}