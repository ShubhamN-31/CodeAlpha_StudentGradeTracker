package ui;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_NAME = "student_db";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root";
    private static final String PASS = "YOUR_PASSWORD_HERE";

    public static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            String sql = "CREATE TABLE IF NOT EXISTS students (" +
                    "StudentID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Name VARCHAR(100), " +
                    "DOB DATE, " +
                    "Department VARCHAR(50), " +
                    "Marks INT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addStudent(String name, String dob, String dept, String marks, String grade) {
        String sql = "INSERT INTO students(Name, DOB, Department, Marks) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dob);
            pstmt.setString(3, dept);
            pstmt.setString(4, marks);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // New Update Method
    public static void updateStudent(int id, String name, String dob, String dept, String marks) {
        String sql = "UPDATE students SET Name=?, DOB=?, Department=?, Marks=? WHERE StudentID=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dob);
            pstmt.setString(3, dept);
            pstmt.setString(4, marks);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE StudentID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<StudentRow> getAllStudents() {
        List<StudentRow> students = new ArrayList<>();
        // Added StudentID to the SELECT query
        String sql = "SELECT StudentID, Name, DOB, Department, Marks FROM students";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("StudentID"); // Get the ID
                String name = rs.getString("Name");
                String dob = rs.getString("DOB");
                String dept = rs.getString("Department");
                String marks = rs.getString("Marks");

                double m = (marks != null && !marks.isEmpty()) ? Double.parseDouble(marks) : 0;
                String grade = (m >= 90) ? "A" : (m >= 75) ? "B" : (m >= 60) ? "C" : "D";

                // Pass the ID to the StudentRow constructor
                students.add(new StudentRow(id, name, dob, dept, marks, grade));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static void clearDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM students");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}