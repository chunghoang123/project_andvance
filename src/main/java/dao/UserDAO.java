package dao;

import model.User;
import util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, department, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getDepartment());
            ps.setString(6, user.getPhone());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi dang ky user: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setDepartment(rs.getString("department"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Loi getUserByUsername: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setDepartment(rs.getString("department"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Loi findById(User): " + e.getMessage());
        }
        return null;
    }

    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, department = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getDepartment());
            ps.setInt(4, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi updateProfile: " + e.getMessage());
            return false;
        }
    }

    public List<User> findAllByRole(String roleName) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, full_name, role, department, phone FROM users WHERE role = ? ORDER BY username";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roleName.toUpperCase());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                u.setDepartment(rs.getString("department"));
                u.setPhone(rs.getString("phone"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Loi findAllByRole: " + e.getMessage());
        }
        return list;
    }

    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, full_name, role, department, phone FROM users ORDER BY username";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                u.setDepartment(rs.getString("department"));
                u.setPhone(rs.getString("phone"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Loi findAllUsers: " + e.getMessage());
        }
        return list;
    }
}
