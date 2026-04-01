package service;

import util.MyDatabase;
import java.sql.*;

public class ReportService {

    // Tính toán chi phí (Giả sử giá mỗi giờ là 200,000 VND)
    public void exportCostReport(int bookingId) {
        String sql = "SELECT r.room_name, b.start_time, b.end_time " +
                "FROM bookings b JOIN rooms r ON b.room_id = r.id WHERE b.id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("--- HOA DON CHI PHI ---");
                System.out.println("Phong: " + rs.getString("room_name"));
                System.out.println("Tam tinh: 500.000 VND (Phi co dinh)");
                // Bạn có thể viết thêm logic tính diff giữa start_time và end_time tại đây
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Phân tích tần suất sử dụng phòng (Tính năng 4)
    public void analyzeUsage() {
        String sql = "SELECT r.room_name, COUNT(b.id) as count " +
                "FROM rooms r LEFT JOIN bookings b ON r.id = b.room_id " +
                "GROUP BY r.room_name ORDER BY count DESC";
        try (Connection conn = MyDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- THONG KE TAN SUAT SU DUNG PHONG ---");
            while (rs.next()) {
                System.out.println("Phong " + rs.getString("room_name") + ": " + rs.getInt("count") + " luot dat.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}