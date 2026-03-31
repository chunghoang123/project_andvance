package service;

import dao.BookingDao;
import dao.ResourceDAO;
import model.Booking;
import model.BookingDetail;
import model.Room;
import util.DateTimeUtil;
import util.MyDatabase;
import util.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private BookingDao bookingDao = new BookingDao();
    private ResourceDAO resourceDAO = new ResourceDAO();

    /**
     * 1. HÀM THỰC THI CHÍNH (Dành cho UI gọi)
     * Kiểm tra toàn bộ điều kiện trước khi lưu vào DB.
     */
    public String executeBooking(Booking b, List<BookingDetail> details, int participants) {
        // --- KIỂM TRA PHÒNG ---
        Room room = resourceDAO.getRoomById(b.getRoomId());
        if (room == null) return "[LOI] Phong khong ton tai!";
        if (room.getStatus() == 0) return "[LOI] Phong dang bao tri!";

        // --- KIỂM TRA SỨC CHỨA ---
        if (participants > room.getCapacity()) {
            return "[LOI] So nguoi vuot qua suc chua (" + room.getCapacity() + ")!";
        }

        // --- KIỂM TRA THỜI GIAN ---
        LocalDateTime start = DateTimeUtil.parse(b.getStartTime());
        LocalDateTime end = DateTimeUtil.parse(b.getEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null) return "[LOI] Dinh dang ngay thang sai!";
        if (start.isBefore(now)) return "[LOI] Khong the dat o thoi gian qua khu!";
        if (!end.isAfter(start.plusMinutes(29))) return "[LOI] Thoi gian thue toi thieu 30 phut!";

        // --- KIỂM TRA TRÙNG LỊCH ---
        if (!bookingDao.isRoomAvailable(b.getRoomId(), b.getStartTime(), b.getEndTime())) {
            return "[LOI] Phong da co nguoi dat trong khung gio nay!";
        }

        // --- LƯU VÀO DB ---
        if (saveBooking(b, details)) {
            Logger.log("User " + b.getUserId() + " dat phong " + room.getRoomName());
            return "[OK] Dat phong thanh cong! Cho Admin duyet.";
        }

        return "[LOI] He thong gap su co khi luu du lieu!";
    }

    /**
     * 2. HÀM LƯU TRANSACTION (Đã sửa lỗi Resource Leak)
     */
    public boolean saveBooking(Booking b, List<BookingDetail> details) {
        String sqlBooking = "INSERT INTO bookings (user_id, room_id, start_time, end_time, status, prep_status) VALUES (?, ?, ?, ?, 'PENDING', 'PREPARING')";
        String sqlDetail = "INSERT INTO booking_details (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";
        String sqlUpdateQty = "UPDATE equipments SET available_qty = available_qty - ? WHERE id = ? AND available_qty >= ?";

        // Sử dụng try-with-resources để tự động đóng Connection & Statement
        try (Connection conn = MyDatabase.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psB = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateQty)) {

                // 1. Lưu Booking chính
                psB.setInt(1, b.getUserId());
                psB.setInt(2, b.getRoomId());
                psB.setString(3, b.getStartTime());
                psB.setString(4, b.getEndTime());

                if (psB.executeUpdate() == 0) throw new SQLException("Loi insert booking");

                ResultSet rs = psB.getGeneratedKeys();
                int generatedId = 0;
                if (rs.next()) generatedId = rs.getInt(1);

                // 2. Lưu chi tiết và trừ kho
                for (BookingDetail d : details) {
                    // Thêm vào bảng chi tiết
                    psDetail.setInt(1, generatedId);
                    psDetail.setInt(2, d.getEquipmentId());
                    psDetail.setInt(3, d.getQuantity());
                    psDetail.addBatch();

                    // Cập nhật kho (Nếu Update trả về 0 dòng -> Thiếu hàng)
                    psUpdate.setInt(1, d.getQuantity());
                    psUpdate.setInt(2, d.getEquipmentId());
                    psUpdate.setInt(3, d.getQuantity());

                    if (psUpdate.executeUpdate() == 0) {
                        throw new SQLException("Khong du so luong thiet bi ID: " + d.getEquipmentId());
                    }
                }
                psDetail.executeBatch();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction Error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CÁC PHƯƠNG THỨC NGHIỆP VỤ KHÁC ---

    // Admin duyệt đơn
    public List<Booking> getAllPending() {
        return bookingDao.findByStatus("PENDING");
    }

    public boolean approveAndAssign(int bId, int sId) {
        return bookingDao.updateStatusAndSupport(bId, "APPROVED", sId);
    }

    // Nhân viên xem lịch sử
    public List<Booking> getHistoryByUserId(int userId) {
        return bookingDao.findByUserId(userId);
    }

    // Support chuẩn bị thiết bị
    public List<Booking> getTasksBySupportId(int supportId) {
        return bookingDao.findBySupportId(supportId);
    }

    public boolean updatePrepStatus(int bId, String status) {
        return bookingDao.updatePrepStatus(bId, status);
    }

    // Hủy đơn (Có logic hoàn trả kho trong DAO)
    public boolean cancelBooking(int bookingId, int userId) {
        return bookingDao.cancelBooking(bookingId, userId);
    }
    public List<Room> searchAvailableRooms(String start, String end) {
        // 1. Kiểm tra định dạng thời gian trước khi tìm
        if (DateTimeUtil.parse(start) == null || DateTimeUtil.parse(end) == null) {
            return new ArrayList<>(); // Trả về danh sách rỗng nếu thời gian sai
        }

        // 2. Gọi DAO để lấy các phòng không bị trùng lịch
        return resourceDAO.getAvailableRooms(start, end);
    }
    public List<Room> getCurrentlyAvailableRooms() {
        List<Room> list = new ArrayList<>();
        // Lấy thời gian hiện tại
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // SQL: Chỉ lấy phòng đang 'Hoat dong' và KHÔNG có lịch đặt trùng với thời điểm 'now'
        String sql = "SELECT * FROM rooms WHERE status = 1 AND id NOT IN (" +
                "  SELECT room_id FROM bookings " +
                "  WHERE status IN ('PENDING', 'APPROVED') " +
                "  AND (? BETWEEN start_time AND end_time)" +
                ")";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, now);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Room(
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
        return list;
    }
}