package service;

import dao.ResourceDAO;
import dao.UserDAO;
import model.Room;
import model.Equipment;
import model.User;
import util.PasswordHash;

import java.util.List;

public class AdminService {
    private ResourceDAO resourceDAO = new ResourceDAO();
    private UserDAO userDAO = new UserDAO();
    private BookingService bookingService = new BookingService();

    // --- QUẢN LÝ TÀI KHOẢN ---
    public String createStaffAccount(User user, String pass) {
        // Tự động băm mật khẩu để đồng bộ với hệ thống đăng nhập
        user.setPassword(PasswordHash.hashPassword(pass));
        if (userDAO.register(user)) {
            return "Thanh cong: Da tao tai khoan " + user.getRole();
        }
        return "That bai: Username da ton tai!";
    }

    // --- QUẢN LÝ PHÒNG ---
    public boolean addRoom(Room room) {
        return resourceDAO.addRoom(room);
    }

    public List<Room> getAllRooms() {
        return resourceDAO.getAllRooms();
    }
    public List<Room> searchRooms(String keyword) {
        return resourceDAO.searchByName(keyword);
    }

    public boolean updateRoom(Room room) {
        // Đã xóa biến 'conn' thừa - ResourceDAO sẽ tự lấy connection
        return resourceDAO.updateRoom(room);
    }

    public boolean deleteRoom(int roomId) {
        // Đã xóa biến 'conn' thừa
        return resourceDAO.deleteRoom(roomId);
    }

    // --- QUẢN LÝ THIẾT BỊ ---
    public boolean addEquipment(Equipment eq) {
        return resourceDAO.addEquipment(eq);
    }

    public List<Equipment> getAllEquipments() {
        return resourceDAO.getAllEquipments();
    }

    public boolean updateEquipment(int id, int total, int avail) {
        // Logic check: Số lượng sẵn có không được lớn hơn tổng số
        if (avail > total) return false;
        return resourceDAO.updateEquipmentQty(id, total, avail);
    }
    public String approveBooking(int bookingId, int supportStaffId) {
        // 1. Kiểm tra xem staff có đúng là role SUPPORT không (Optional)
        // 2. Gọi DAO cập nhật
        if (bookingService.approveAndAssign(bookingId, supportStaffId)) {
            return "Duyet thanh cong! Da giao viec cho Staff ID: " + supportStaffId;
        }
        return "Loi: Khong tim thay Booking hoac Staff.";
    }
    public List<User> getAllSupportStaff() {
        // Gọi sang UserDAO hoặc AuthService tùy cấu trúc của bạn
        return userDAO.findAllByRole("SUPPORT");
    }
}