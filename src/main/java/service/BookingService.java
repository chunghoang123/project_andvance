package service;

import dao.BookingDao;
import dao.ResourceDAO;
import dao.UserDAO;
import model.Booking;
import model.BookingDetail;
import model.Room;
import model.User;
import util.DateTimeUtil;
import util.Logger;
import util.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private final BookingDao bookingDao = new BookingDao();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final UserDAO userDAO = new UserDAO();

    public String executeBooking(Booking b, List<BookingDetail> details, int participants) {
        if (b == null) return "[LOI] Du lieu booking khong hop le!";
        if (participants <= 0) return "[LOI] So nguoi tham gia phai > 0!";

        Room room = resourceDAO.getRoomById(b.getRoomId());
        if (room == null) return "[LOI] Phong khong ton tai!";
        if (room.getStatus() == 0) return "[LOI] Phong dang bao tri!";
        if (participants > room.getCapacity()) {
            return "[LOI] So nguoi vuot qua suc chua cua phong (" + room.getCapacity() + ")!";
        }

        LocalDateTime start = DateTimeUtil.parse(b.getStartTime());
        LocalDateTime end = DateTimeUtil.parse(b.getEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null) return "[LOI] Sai dinh dang thoi gian (yyyy-MM-dd HH:mm)!";
        if (!end.isAfter(start)) return "[LOI] Thoi gian ket thuc phai sau thoi gian bat dau!";
        if (start.isBefore(now)) return "[LOI] Khong the dat phong trong qua khu!";
        if (!end.isAfter(start.plusMinutes(29))) return "[LOI] Thoi gian dat toi thieu la 30 phut!";

        if (!bookingDao.isRoomAvailable(b.getRoomId(), b.getStartTime(), b.getEndTime())) {
            return "[LOI] Phong da bi trung lich trong khung gio nay!";
        }

        boolean ok = saveBooking(b, details == null ? new ArrayList<>() : details);
        if (ok) {
            Logger.log("User " + b.getUserId() + " gui booking phong " + room.getRoomName());
            return "[OK] Dat phong thanh cong! Trang thai: PENDING";
        }
        return "[LOI] Khong the luu booking vao CSDL!";
    }

    public boolean saveBooking(Booking b, List<BookingDetail> details) {
        String sqlBooking = "INSERT INTO bookings (user_id, room_id, start_time, end_time, status, prep_status) " +
                "VALUES (?, ?, ?, ?, 'PENDING', 'PREPARING')";
        String sqlDetail = "INSERT INTO booking_details (booking_id, equipment_id, service_id, quantity) VALUES (?, ?, ?, ?)";
        String sqlUpdateEq = "UPDATE equipments SET available_qty = available_qty - ? WHERE id = ? AND available_qty >= ?";

        try (Connection conn = MyDatabase.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psBooking = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psEq = conn.prepareStatement(sqlUpdateEq)) {

                psBooking.setInt(1, b.getUserId());
                psBooking.setInt(2, b.getRoomId());
                psBooking.setString(3, b.getStartTime());
                psBooking.setString(4, b.getEndTime());

                if (psBooking.executeUpdate() <= 0) throw new SQLException("Insert booking that bai!");

                int bookingId;
                try (ResultSet rs = psBooking.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Khong lay duoc booking_id!");
                    bookingId = rs.getInt(1);
                }

                for (BookingDetail d : details) {
                    if (d == null || d.getQuantity() <= 0) continue;

                    psDetail.setInt(1, bookingId);

                    if (d.getEquipmentId() > 0) psDetail.setInt(2, d.getEquipmentId());
                    else psDetail.setNull(2, Types.INTEGER);

                    if (d.getServiceId() > 0) psDetail.setInt(3, d.getServiceId());
                    else psDetail.setNull(3, Types.INTEGER);

                    psDetail.setInt(4, d.getQuantity());
                    psDetail.addBatch();

                    if (d.getEquipmentId() > 0) {
                        psEq.setInt(1, d.getQuantity());
                        psEq.setInt(2, d.getEquipmentId());
                        psEq.setInt(3, d.getQuantity());

                        if (psEq.executeUpdate() == 0) {
                            throw new SQLException("Khong du so luong thiet bi ID " + d.getEquipmentId());
                        }
                    }
                }

                psDetail.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Transaction booking rollback: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Loi saveBooking: " + e.getMessage());
            return false;
        }
    }

    public List<Booking> getAllPending() {
        return bookingDao.findByStatus("PENDING");
    }

    public boolean approveAndAssign(int bookingId, int supportStaffId) {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) return false;
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) return false;

        User support = userDAO.findById(supportStaffId);
        if (support == null || !"SUPPORT".equalsIgnoreCase(support.getRole())) return false;

        if (bookingDao.hasRoomConflictExcludingBooking(
                bookingId,
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime())) {
            return false;
        }

        return bookingDao.updateStatusAndSupport(bookingId, "APPROVED", supportStaffId);
    }

    public boolean rejectBooking(int bookingId) {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) return false;
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) return false;
        return bookingDao.rejectBooking(bookingId);
    }

    public List<Booking> getHistoryByUserId(int userId) {
        return bookingDao.findByUserId(userId);
    }

    public boolean cancelBooking(int bookingId, int userId) {
        return bookingDao.cancelBooking(bookingId, userId);
    }

    public List<Booking> getTasksBySupportId(int supportId) {
        return bookingDao.findBySupportId(supportId);
    }

    public boolean updatePrepStatus(int bookingId, String prepStatus) {
        return bookingDao.updatePrepStatus(bookingId, prepStatus);
    }

    public boolean isUserConflicting(int userId, String startTime, String endTime) {
        return bookingDao.hasUserConflict(userId, startTime, endTime);
    }

    public List<Room> searchAvailableRooms(String startTime, String endTime) {
        return resourceDAO.getAvailableRooms(startTime, endTime);
    }
}
