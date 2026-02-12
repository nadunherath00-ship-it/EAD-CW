package util;

import model.User;

/**
 * Session Manager - Singleton Pattern
 * Manages current logged-in user session
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private long loginTime;
    
    // Private constructor
    private SessionManager() {
    }
    
    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Login user and create session
     */
    public void login(User user) {
        this.currentUser = user;
        this.loginTime = System.currentTimeMillis();
    }
    
    /**
     * Logout user and clear session
     */
    public void logout() {
        this.currentUser = null;
        this.loginTime = 0;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get username of current user
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    /**
     * Get full name of current user
     */
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }
    
    /**
     * Get role of current user
     */
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    /**
     * Check if current user has admin role
     */
    public boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && role.equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Get session duration in minutes
     */
    public long getSessionDuration() {
        if (loginTime > 0) {
            return (System.currentTimeMillis() - loginTime) / 1000 / 60;
        }
        return 0;
    }
}