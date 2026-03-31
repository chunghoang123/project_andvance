package dao;

import model.Room;
import util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    // Thêm phòng mới (Yêu cầu 3.2)
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_name, capacity, location, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getLocation());
            pstmt.setInt(4, room.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL Add Room: " + e.getMessage());
            return false;
        }
    }

    // Lấy danh sách tất cả các phòng
    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = MyDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("id"),
                        rs.getString("room_name"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getInt("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // Xóa phòng theo ID
    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}