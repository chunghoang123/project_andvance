package dao;

import util.MyDatabase;
import java.sql.*;

public class ReportDAO {
    // Tính tổng tiền cho một Booking (Tính năng 2)
    public double calculateBookingCost(int bookingId) {
        String sql = "SELECT r.price_per_hour, b.start_time, b.end_time " +
                "FROM bookings b JOIN rooms r ON b.room_id = r.id WHERE b.id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Giả sử tính theo giờ (đơn giản hóa)
                return rs.getDouble("price_per_hour") * 2; // Ví dụ mặc định 2h
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Thống kê số lần phòng được đặt (Tính năng 4)
    public void printUsageReport() {
        String sql = "SELECT r.room_name, COUNT(b.id) as total_bookings " +
                "FROM rooms r LEFT JOIN bookings b ON r.id = b.room_id " +
                "GROUP BY r.id";
        // Thực thi và in kết quả...
    }
}