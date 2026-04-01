package service;

import util.MyDatabase;
import java.sql.*;

public class FeedbackService {
    public boolean sendFeedback(int bookingId, int rating, String comment) {
        String sql = "INSERT INTO feedbacks (booking_id, rating, comment) VALUES (?, ?, ?)";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, rating);
            ps.setString(3, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi gui feedback: " + e.getMessage());
            return false;
        }
    }
}