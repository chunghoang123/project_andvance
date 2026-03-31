package dao;

import model.Booking;
import model.BookingDetail;
import model.Room;
import util.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    // 1. Kiểm tra xung đột thời gian (Ngày 3)
    public boolean isRoomAvailable(int roomId, String start, String end) {
        // Thuật toán Overlap: (Bắt đầu mới < Kết thúc cũ) VÀ (Kết thúc mới > Bắt đầu cũ)
        // Chỉ check những đơn PENDING (Chờ duyệt) hoặc APPROVED (Đã duyệt)
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? " +
                "AND status IN ('PENDING', 'APPROVED') " +
                "AND start_time < ? AND end_time > ?";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setString(2, end);   // Kết thúc mới
            ps.setString(3, start); // Bắt đầu mới

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Nếu COUNT > 0 nghĩa là đã có ít nhất 1 lịch trùng -> Trả về false (Không sẵn sàng)
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Mặc định báo bận nếu có lỗi SQL
    }

    // 2. Lưu Booking và Details bằng Transaction (Ngày 3)
    public boolean saveBooking(Booking b, List<BookingDetail> details) {
        Connection conn = null;
        try {
            conn = MyDatabase.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // 1. Lưu Booking chính
            String sqlB = "INSERT INTO bookings (user_id, room_id, start_time, end_time, status, prep_status) " +
                    "VALUES (?, ?, ?, ?, 'PENDING', 'PREPARING')";
            PreparedStatement psB = conn.prepareStatement(sqlB, Statement.RETURN_GENERATED_KEYS);
            psB.setInt(1, b.getUserId());
            psB.setInt(2, b.getRoomId());
            psB.setString(3, b.getStartTime());
            psB.setString(4, b.getEndTime());
            psB.executeUpdate();

            ResultSet rs = psB.getGeneratedKeys();
            int bId = 0;
            if (rs.next()) bId = rs.getInt(1);

            // 2. Chuẩn bị SQL cho Chi tiết và Cập nhật kho
            String sqlD = "INSERT INTO booking_details (booking_id, equipment_id, service_id, quantity) VALUES (?, ?, ?, ?)";
            // QUAN TRỌNG: Chỉ trừ vào cột available_qty và kiểm tra điều kiện đủ hàng
            String sqlUpdateQty = "UPDATE equipments SET available_qty = available_qty - ? WHERE id = ? AND available_qty >= ?";

            PreparedStatement psD = conn.prepareStatement(sqlD);
            PreparedStatement psU = conn.prepareStatement(sqlUpdateQty);

            for (BookingDetail d : details) {
                // Lưu chi tiết
                psD.setInt(1, bId);
                if (d.getEquipmentId() > 0) psD.setInt(2, d.getEquipmentId()); else psD.setNull(2, java.sql.Types.INTEGER);
                if (d.getServiceId() > 0) psD.setInt(3, d.getServiceId()); else psD.setNull(3, java.sql.Types.INTEGER);
                psD.setInt(4, d.getQuantity());
                psD.addBatch();

                // Cập nhật kho (Nếu là thiết bị - Equipment)
                if (d.getEquipmentId() > 0) {
                    psU.setInt(1, d.getQuantity());
                    psU.setInt(2, d.getEquipmentId());
                    psU.setInt(3, d.getQuantity()); // Điều kiện chặn: available_qty >= quantity

                    int rowsAffected = psU.executeUpdate();
                    if (rowsAffected == 0) {
                        // Nếu không có dòng nào bị ảnh hưởng -> Nghĩa là không đủ hàng trong kho
                        throw new SQLException("Loi: Thiet bi ID " + d.getEquipmentId() + " khong du so luong san co!");
                    }
                }
            }
            psD.executeBatch();

            conn.commit(); // Chốt mọi thay đổi xuống Database
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            System.err.println("Transaction Rollback: " + e.getMessage());
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 3. Tìm kiếm theo trạng thái (Dùng cho Admin lấy danh sách PENDING)
    public List<Booking> findByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = ? ORDER BY id DESC";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 4. Duyệt và phân công nhân viên hỗ trợ (Ngày 4)
    public boolean updateStatusAndSupport(int id, String status, int supportId) {
        String sql = "UPDATE bookings SET status = ?, support_id = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, supportId);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // 5. Tìm lịch sử đặt phòng của một nhân viên (Ngày 3)
    public List<Booking> findByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 6. Tìm các công việc được giao cho nhân viên hỗ trợ (Ngày 4)
    public List<Booking> findBySupportId(int supportId) {
        List<Booking> list = new ArrayList<>();
        // LƯU Ý: Phải khớp chính xác chữ 'APPROVED' (viết hoa) như khi Admin duyệt
        String sql = "SELECT * FROM bookings WHERE support_id = ? AND status = 'APPROVED' ORDER BY id DESC";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supportId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBooking(rs)); // Hàm mapRowToBooking bạn đã viết thấu đáo rồi
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // 7. Cập nhật tình trạng chuẩn bị (Ngày 4)
    public boolean updatePrepStatus(int id, String prepStatus) {
        String sql = "UPDATE bookings SET prep_status = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prepStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // --- Hàm phụ trợ để chuyển đổi dữ liệu từ Database sang Object ---
    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
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
    public List<Booking> findTasksByStaff(int staffId) {
        List<Booking> list = new ArrayList<>();
        // Chi lay nhung phong da duoc duyet (APPROVED) va giao cho minh
        String sql = "SELECT * FROM bookings WHERE support_id = ? AND status = 'APPROVED'";
        // ... thực thi JDBC tương tự các hàm trước ...
        return list;
    }
    public boolean cancelBooking(int bId, int uId) {
        Connection conn = null;
        try {
            conn = MyDatabase.getConnection();
            conn.setAutoCommit(false); // Dùng Transaction để vừa cộng kho vừa xóa

            // 1. Cộng trả lại số lượng thiết bị vào kho trước khi xóa
            String sqlRestore = "UPDATE equipments e " +
                    "JOIN booking_details bd ON e.id = bd.equipment_id " +
                    "SET e.available_qty = e.available_qty + bd.quantity " +
                    "WHERE bd.booking_id = ?";
            PreparedStatement psRestore = conn.prepareStatement(sqlRestore);
            psRestore.setInt(1, bId);
            psRestore.executeUpdate();

            // 2. Xóa Booking (Chi tiết sẽ tự xóa nếu bạn để On Delete Cascade trong DB,
            // nếu không bạn phải xóa booking_details trước)
            String sqlDel = "DELETE FROM bookings WHERE id = ? AND user_id = ? AND status = 'PENDING'";
            PreparedStatement psDel = conn.prepareStatement(sqlDel);
            psDel.setInt(1, bId);
            psDel.setInt(2, uId);

            int row = psDel.executeUpdate();
            conn.commit();
            return row > 0;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return false;
        }
    }
    public boolean decreaseEquipmentQty(int id, int qty) {
        // Lệnh SQL trừ đi số lượng đang có
        String sql = "UPDATE equipments SET available_qty = available_qty - ? WHERE id = ? AND available_qty >= ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, id);
            ps.setInt(3, qty); // Đảm bảo không trừ quá số lượng đang có
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Room> getAvailableRooms(String startTime, String endTime) {
        List<Room> list = new ArrayList<>();
        // SQL: Chọn các phòng KHÔNG nằm trong danh sách các phòng đã bị đặt trùng lịch
        String sql = "SELECT * FROM rooms WHERE status = 1 AND id NOT IN (" +
                "  SELECT room_id FROM bookings " +
                "  WHERE status IN ('PENDING', 'APPROVED') " +
                "  AND (start_time < ? AND end_time > ?)" +
                ")";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, endTime);   // Kết thúc của khung giờ đang tìm
            ps.setString(2, startTime); // Bắt đầu của khung giờ đang tìm

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Room r = new Room();
                r.setId(rs.getInt("id"));
                r.setRoomName(rs.getString("room_name"));
                r.setCapacity(rs.getInt("capacity"));
                r.setStatus(rs.getInt("status"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}