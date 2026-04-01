package dao;

import model.Booking;
import util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    public boolean isRoomAvailable(int roomId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE room_id = ? AND status IN ('PENDING', 'APPROVED') " +
                "AND start_time < ? AND end_time > ?";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setString(2, end);
            ps.setString(3, start);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Loi isRoomAvailable: " + e.getMessage());
        }
        return false;
    }

    public boolean hasRoomConflictExcludingBooking(int bookingId, int roomId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE id <> ? AND room_id = ? AND status IN ('PENDING', 'APPROVED') " +
                "AND start_time < ? AND end_time > ?";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, roomId);
            ps.setString(3, end);
            ps.setString(4, start);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Loi hasRoomConflictExcludingBooking: " + e.getMessage());
        }
        return true;
    }

    public Booking findById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Loi findById(Booking): " + e.getMessage());
        }
        return null;
    }

    public List<Booking> findByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = ? ORDER BY id DESC";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Loi findByStatus: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> findByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY id DESC";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Loi findByUserId: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> findBySupportId(int supportId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE support_id = ? AND status = 'APPROVED' ORDER BY start_time ASC";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supportId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Loi findBySupportId: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatusAndSupport(int id, String status, Integer supportId) {
        String sql = "UPDATE bookings SET status = ?, support_id = ? WHERE id = ? AND status = 'PENDING'";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            if (supportId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, supportId);
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi updateStatusAndSupport: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectBooking(int id) {
        return updateStatusAndSupport(id, "REJECTED", null);
    }

    public boolean updatePrepStatus(int id, String prepStatus) {
        String sql = "UPDATE bookings SET prep_status = ? WHERE id = ? AND status = 'APPROVED'";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prepStatus);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi updatePrepStatus: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelBooking(int bookingId, int userId) {
        Connection conn = null;
        try {
            conn = MyDatabase.getConnection();
            conn.setAutoCommit(false);

            // 1. Vérifier que le booking existe, appartient à l'user, et est PENDING
            String sqlCheck = "SELECT status FROM bookings WHERE id = ? AND user_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next() || !"PENDING".equalsIgnoreCase(rs.getString("status"))) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Restaurer les équipements
            String sqlRestore = "UPDATE equipments e " +
                    "JOIN booking_details bd ON e.id = bd.equipment_id " +
                    "SET e.available_qty = e.available_qty + bd.quantity " +
                    "WHERE bd.booking_id = ? AND bd.equipment_id IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sqlRestore)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // 3. Marquer le booking comme CANCELLED
            String sqlCancel = "UPDATE bookings " +
                    "SET status = 'CANCELLED' " +
                    "WHERE id = ? AND user_id = ? AND status = 'PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(sqlCancel)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);

                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignored) {}
            System.err.println("Loi cancelBooking: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }




    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setStartTime(rs.getString("start_time"));
        b.setEndTime(rs.getString("end_time"));
        b.setStatus(rs.getString("status"));
        b.setPrepStatus(rs.getString("prep_status"));
        b.setSupportId(rs.getInt("support_id"));
        return b;
    }

    public boolean hasUserConflict(int userId, String startTime, String endTime) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE user_id = ? AND status IN ('PENDING', 'APPROVED') " +
                "AND start_time < ? AND end_time > ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, endTime);
            ps.setString(3, startTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Loi hasUserConflict: " + e.getMessage());
        }
        return false;
    }
}
