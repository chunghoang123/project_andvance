package service;

import dao.ResourceDAO;
import dao.UserDAO;
import model.Equipment;
import model.Room;
import model.User;
import util.MyDatabase;
import util.PasswordHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookingService bookingService = new BookingService();

    // ================= USER =================
    public String createStaffAccount(User user, String pass) {
        if (user == null) return "[LOI] Du lieu tai khoan khong hop le!";

        String username = safe(user.getUsername());
        String fullName = safe(user.getFullName());
        String role = safe(user.getRole()).toUpperCase();

        if (username.isEmpty()) return "[LOI] Username khong duoc de trong!";
        if (fullName.isEmpty()) return "[LOI] Ho ten khong duoc de trong!";
        if (!role.equals("ADMIN") && !role.equals("SUPPORT")) {
            return "[LOI] Role chi duoc la ADMIN hoac SUPPORT!";
        }
        if (pass == null || pass.trim().length() < 6) {
            return "[LOI] Mat khau phai co it nhat 6 ky tu!";
        }
        if (userDAO.usernameExists(username)) {
            return "[LOI] Username da ton tai!";
        }

        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPassword(PasswordHash.hashPassword(pass.trim()));

        return userDAO.register(user)
                ? "[OK] Da tao tai khoan " + role + " thanh cong!"
                : "[LOI] Tao tai khoan that bai!";
    }

    public List<User> getAllSupportStaff() {
        return userDAO.findAllByRole("SUPPORT");
    }

    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }

    // ================= ROOM =================
    public List<Room> getAllRooms() {
        return resourceDAO.getAllRooms();
    }

    public List<Room> searchRooms(String keyword) {
        return resourceDAO.searchByName(keyword == null ? "" : keyword.trim());
    }

    public Room getRoomById(int id) {
        return resourceDAO.getRoomById(id);
    }

    public String addRoom(Room room) {
        if (room == null) return "[LOI] Du lieu phong khong hop le!";
        if (safe(room.getRoomName()).isEmpty()) return "[LOI] Ten phong khong duoc de trong!";
        if (room.getCapacity() <= 0) return "[LOI] Suc chua phai > 0!";
        if (safe(room.getLocation()).isEmpty()) return "[LOI] Vi tri khong duoc de trong!";

        for (Room r : getAllRooms()) {
            if (r.getRoomName() != null && r.getRoomName().trim().equalsIgnoreCase(room.getRoomName().trim())) {
                return "[LOI] Ten phong da ton tai!";
            }
        }

        return resourceDAO.addRoom(room)
                ? "[OK] Them phong thanh cong!"
                : "[LOI] Them phong that bai!";
    }

    public String updateRoom(Room room) {
        if (room == null || room.getId() <= 0) return "[LOI] ID phong khong hop le!";
        Room old = resourceDAO.getRoomById(room.getId());
        if (old == null) return "[LOI] Khong tim thay phong!";

        if (safe(room.getRoomName()).isEmpty()) return "[LOI] Ten phong khong duoc de trong!";
        if (room.getCapacity() <= 0) return "[LOI] Suc chua phai > 0!";
        if (safe(room.getLocation()).isEmpty()) return "[LOI] Vi tri khong duoc de trong!";

        for (Room r : getAllRooms()) {
            if (r.getId() != room.getId()
                    && r.getRoomName() != null
                    && r.getRoomName().trim().equalsIgnoreCase(room.getRoomName().trim())) {
                return "[LOI] Ten phong da ton tai!";
            }
        }

        return resourceDAO.updateRoom(room)
                ? "[OK] Cap nhat phong thanh cong!"
                : "[LOI] Cap nhat phong that bai!";
    }

    public String deleteRoom(int roomId) {
        if (roomId <= 0) return "[LOI] ID phong khong hop le!";
        if (resourceDAO.getRoomById(roomId) == null) return "[LOI] Phong khong ton tai!";

        return resourceDAO.deleteRoom(roomId)
                ? "[OK] Xoa phong thanh cong!"
                : "[LOI] Khong the xoa phong. Co the phong dang duoc rang buoc boi booking!";
    }

    // ================= EQUIPMENT =================
    public List<Equipment> getAllEquipments() {
        return resourceDAO.getAllEquipments();
    }

    public Equipment getEquipmentById(int id) {
        for (Equipment e : getAllEquipments()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public String addEquipment(Equipment eq) {
        if (eq == null) return "[LOI] Du lieu thiet bi khong hop le!";
        if (safe(eq.getName()).isEmpty()) return "[LOI] Ten thiet bi khong duoc de trong!";
        if (eq.getTotalQty() <= 0) return "[LOI] Tong so luong phai > 0!";
        if (eq.getAvailableQty() < 0 || eq.getAvailableQty() > eq.getTotalQty()) {
            return "[LOI] So luong san co khong hop le!";
        }

        for (Equipment e : getAllEquipments()) {
            if (e.getName() != null && e.getName().trim().equalsIgnoreCase(eq.getName().trim())) {
                return "[LOI] Ten thiet bi da ton tai!";
            }
        }

        return resourceDAO.addEquipment(eq)
                ? "[OK] Them thiet bi thanh cong!"
                : "[LOI] Them thiet bi that bai!";
    }

    public String updateEquipment(Equipment eq) {
        if (eq == null || eq.getId() <= 0) return "[LOI] ID thiet bi khong hop le!";
        if (getEquipmentById(eq.getId()) == null) return "[LOI] Khong tim thay thiet bi!";
        if (safe(eq.getName()).isEmpty()) return "[LOI] Ten thiet bi khong duoc de trong!";
        if (eq.getTotalQty() <= 0) return "[LOI] Tong so luong phai > 0!";
        if (eq.getAvailableQty() < 0 || eq.getAvailableQty() > eq.getTotalQty()) {
            return "[LOI] So luong san co khong hop le!";
        }

        for (Equipment e : getAllEquipments()) {
            if (e.getId() != eq.getId()
                    && e.getName() != null
                    && e.getName().trim().equalsIgnoreCase(eq.getName().trim())) {
                return "[LOI] Ten thiet bi da ton tai!";
            }
        }

        String sql = "UPDATE equipments SET name = ?, total_qty = ?, available_qty = ? WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, eq.getName().trim());
            ps.setInt(2, eq.getTotalQty());
            ps.setInt(3, eq.getAvailableQty());
            ps.setInt(4, eq.getId());

            return ps.executeUpdate() > 0
                    ? "[OK] Cap nhat thiet bi thanh cong!"
                    : "[LOI] Cap nhat thiet bi that bai!";
        } catch (SQLException e) {
            return "[LOI] Loi SQL khi cap nhat thiet bi: " + e.getMessage();
        }
    }

    public String deleteEquipment(int id) {
        if (id <= 0) return "[LOI] ID thiet bi khong hop le!";
        if (getEquipmentById(id) == null) return "[LOI] Thiet bi khong ton tai!";

        String sql = "DELETE FROM equipments WHERE id = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0
                    ? "[OK] Xoa thiet bi thanh cong!"
                    : "[LOI] Xoa thiet bi that bai!";
        } catch (SQLException e) {
            return "[LOI] Khong the xoa thiet bi. Co the du lieu dang duoc rang buoc!";
        }
    }

    // ================= BOOKING APPROVAL =================
    public String approveBooking(int bookingId, int supportStaffId) {
        return bookingService.approveAndAssign(bookingId, supportStaffId)
                ? "[OK] Da duyet booking va phan cong staff!"
                : "[LOI] Duyet booking that bai!";
    }

    public String rejectBooking(int bookingId) {
        return bookingService.rejectBooking(bookingId)
                ? "[OK] Da tu choi booking!"
                : "[LOI] Tu choi booking that bai!";
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
