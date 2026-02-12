package model;

public class Course {
    
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credits;
    private String semester;
    private int lecturerId;
    private String lecturerName;
    private int capacity;
    private String description;

    public Course() {
    }
    
    public Course(String courseCode, String courseName, int credits, 
                  String semester, int lecturerId, int capacity, String description) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.semester = semester;
        this.lecturerId = lecturerId;
        this.capacity = capacity;
        this.description = description;
    }

    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        this.credits = credits;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public int getLecturerId() {
        return lecturerId;
    }
    
    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
    }
    
    public String getLecturerName() {
        return lecturerName;
    }
    
    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}