package dao;

import model.Room;
import model.Equipment;
import util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    // --- QUAN LY PHONG (ROOM) ---
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_name, capacity, location, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getLocation());
            pstmt.setInt(4, room.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    // Hàm Sửa Room
    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_name = ?, capacity = ?, location = ?, status = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setInt(4, room.getStatus());
            ps.setInt(5, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm Xóa Room
    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Khong the xoa phong dang co du lieu lien quan (Booking)!");
            return false;
        }
    }
    public List<Room> searchByName(String name) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_name LIKE ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%"); // Dấu % giúp tìm kiếm tương đối
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Room(rs.getInt("id"), rs.getString("room_name"),
                        rs.getInt("capacity"), rs.getString("location"), rs.getInt("status")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = MyDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Room(rs.getInt("id"), rs.getString("room_name"),
                        rs.getInt("capacity"), rs.getString("location"), rs.getInt("status")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Room getRoomById(int id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Room(
                        rs.getInt("id"),
                        rs.getString("room_name"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getInt("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy ID
    }

    // --- QUAN LY THIET BI (EQUIPMENT) ---
    public boolean addEquipment(Equipment eq) {
        String sql = "INSERT INTO equipments (name, total_qty, available_qty) VALUES (?, ?, ?)";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eq.getName());
            pstmt.setInt(2, eq.getTotalQty());
            pstmt.setInt(3, eq.getAvailableQty());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Equipment> getAllEquipments() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM equipments";
        try (Connection conn = MyDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Equipment(rs.getInt("id"), rs.getString("name"),
                        rs.getInt("total_qty"), rs.getInt("available_qty")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateEquipmentQty(int id, int total, int available) {
        String sql = "UPDATE equipments SET total_qty = ?, available_qty = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, total);
            pstmt.setInt(2, available);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Room> getAvailableRooms(String startTime, String endTime) {
        List<Room> availableRooms = new ArrayList<>();
        // Find rooms that are NOT booked during the specified time period
        String sql = "SELECT r.* FROM rooms r WHERE r.status = 1 AND r.id NOT IN (" +
                     "SELECT DISTINCT b.room_id FROM bookings b " +
                     "WHERE b.status IN ('PENDING', 'APPROVED') " +
                     "AND ((b.start_time <= ? AND b.end_time > ?) " +
                     "OR (b.start_time < ? AND b.end_time >= ?) " +
                     "OR (b.start_time >= ? AND b.end_time <= ?)))";
        
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, startTime);
            ps.setString(2, startTime);
            ps.setString(3, endTime);
            ps.setString(4, endTime);
            ps.setString(5, startTime);
            ps.setString(6, endTime);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                availableRooms.add(new Room(
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
        return availableRooms;
    }

}
