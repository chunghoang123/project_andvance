package presentation;

import model.*;
import service.*;
import util.InputValidator;

import java.util.*;


public class MenuConsole {
    private final Scanner sc = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final AdminService adminService = new AdminService();
    private final BookingService bookingService = new BookingService();
    private User currentUser = null;

    // ================== UI HELPERS ==================
    private void header(String title) {
        System.out.println("\n=============================================");
        System.out.printf("|           %-30s  |\n", title);
        System.out.println("=============================================");
        System.out.printf("| %-8s | %-30s |\n", "Lua chon", "Ten chuc nang");
        System.out.println("---------------------------------------------");
    }
    private void simpleHeader(String title) {
        System.out.println("\n=============================================");
        System.out.printf("|           %-30s  |\n", title);
        System.out.println("=============================================");
    }

    private void line(String left, String right) {
        System.out.printf("| %-8s | %-30s |\n", left, right);
    }

    private void footer() {
        System.out.println("---------------------------------------------");
        System.out.print("===> Lua chon: ");
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Loi: Vui long nhap SO NGUYEN!");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ================== HE THONG CHINH ==================
    public void start() {
        while (true) {
            header("HE THONG DAT PHONG HOP");
            line("  [1]", "Dang nhap");
            line("  [2]", "Dang ky (Employee)");
            line("  [0]", "Thoat");
            footer();

            int choice = readInt("");
            if (choice == 1) handleLogin();
            else if (choice == 2) handleRegister();
            else if (choice == 0) {
                System.out.println("Tam biet!");
                break;
            }
        }
    }

    private void handleLogin() {
        String user = readString("Username: ");
        String pass = readString("Password: ");
        currentUser = authService.login(user, pass);

        if (currentUser != null) {
            System.out.println("Dang nhap thanh cong! Chao " + currentUser.getFullName());
            switch (currentUser.getRole().toUpperCase()) {
                case "ADMIN": showAdminMenu(); break;
                case "EMPLOYEE": showEmployeeMenu(); break;
                case "SUPPORT": showSupportMenu(); break;
            }
        } else {
            System.out.println("Sai tai khoan hoac mat khau!");
        }
    }

    private void handleRegister() {
        simpleHeader("DANG KY TAI KHOAN");
        String u = readString("Username: ");

        // Validate mật khẩu ngay tại lúc nhập
        String p;
        while (true) {
            p = readString("Password (Toi thieu 6 ky tu): ");
            if (p.length() >= 6) {
                break; // Thỏa mãn điều kiện thì thoát vòng lặp
            }
            System.out.println("[LOI] Mat khau qua ngan! Vui long nhap lai.");
        }

        String name = readString("Ho ten: ");

        User newUser = new User();
        newUser.setUsername(u);
        newUser.setFullName(name);
        newUser.setRole("EMPLOYEE");

        System.out.println(authService.register(newUser, p));
    }

    // ================== MENU ADMIN ==================
    private void showAdminMenu() {
        while (true) {
            simpleHeader("MENU QUAN TRI (ADMIN)");
            line("  [1]", "Quan ly phong hop");
            line("  [2]", "Quan ly thiet bi");
            line("  [3]", "Tao tai khoan Staff");
            line("  [4]", "Duyet & Phan cong Booking");
            line("  [0]", "Dang xuat");
            footer();

            int choice = readInt("");
            if (choice == 0) { currentUser = null; break; }
            switch (choice) {
                case 1: renderRoomManagement(); break;
                case 2: handleEquipmentManager(); break;
                case 3: handleCreateStaff(); break;
                case 4: handleAdminApproval(); break;
            }
        }
    }

    private void handleAdminApproval() {
        System.out.println("\n--- DANH SACH YEU CAU CHO DUYET (PENDING) ---");
        List<Booking> pendings = bookingService.getAllPending();
        if (pendings.isEmpty()) {
            System.out.println("Khong co yeu cau nao dang cho.");
            return;
        }

        // Hiển thị danh sách Booking đang chờ
        System.out.printf("%-5s | %-10s | %-10s | %-20s\n", "ID", "User ID", "Room ID", "Thoi gian");
        for (Booking b : pendings) {
            System.out.printf("%-5d | %-10d | %-10d | %s -> %s\n",
                    b.getId(), b.getUserId(), b.getRoomId(), b.getStartTime(), b.getEndTime());
        }

        int bId = readInt("\nNhap ID Booking muon duyet: ");

        // --- BUOC QUAN TRONG: Hien thi danh sach Staff Support ---
        System.out.println("\n--- DANH SACH NHAN VIEN HO TRO (SUPPORT) ---");
        System.out.printf("%-5s | %-20s | %-15s\n", "ID", "Ho Ten", "Vai tro");
        System.out.println("----------------------------------------------");

        // Bạn cần viết thêm hàm getAllSupportStaff trong AdminService
        List<User> supportList = adminService.getAllSupportStaff();

        if (supportList.isEmpty()) {
            System.out.println("(!) Canh bao: Chua co tai khoan SUPPORT nao trong he thong!");
        } else {
            for (User s : supportList) {
                System.out.printf("%-5d | %-20s | %-15s\n",
                        s.getId(), s.getFullName(), s.getRole());
            }
        }
        System.out.println("----------------------------------------------");

        int sId = readInt("Nhap ID Staff ho tro tu danh sach tren: ");

        if (bookingService.approveAndAssign(bId, sId)) {
            System.out.println("Da duyet va phan cong cho Staff ID: " + sId);
        } else {
            System.out.println("That bai! Vui long kiem tra lai ID Booking hoac Staff.");
        }
    }

    // ================== MENU EMPLOYEE ==================
    private void showEmployeeMenu() {
        while (true) {
            header("MENU NHAN VIEN (EMPLOYEE)");
            line("  [1]", "Xem phong trong");
            line("  [2]", "Tim kiem phong (Checklist)"); // Thêm nút
            line("  [3]", "Dat phong moi");
            line("  [4]", "Lich su dat phong");
            line("  [5]", "Huy yeu cau PENDING (Checklist)"); // Thêm nút
            line("  [6]", "Cap nhat ho so (Checklist)"); // Thêm nút
            line("  [0]", "Dang xuat");
            footer();

            int c = readInt("");
            if (c == 0) { currentUser = null; break; }
            switch (c) {
                case 1: renderAvailableRooms(); break;
                case 2: handleSearchRoom(); break;      // Gọi hàm bạn đã viết
                case 3: handleBookingRequest(); break;
                case 4: renderMyHistory(); break;
                case 5: handleCancelBooking(); break;    // Gọi hàm bạn đã viết
                case 6: handleUpdateProfile(); break;   // Gọi hàm mới thêm ở mục 1
            }
        }
    }
    private void handleBookingRequest() {
        simpleHeader("GUI YEU CAU DAT PHONG");

        // BƯỚC 1: Hiển thị danh sách phòng kèm sức chứa để người dùng chọn
        renderAvailableRooms();

        int rId = InputValidator.readInt("Nhap ID phong muon dat: ");
        int participants = InputValidator.readInt("So nguoi tham gia (De kiem tra suc chua): ");

        System.out.println("(!) Dinh dang thoi gian: yyyy-MM-dd HH:mm");
        String start = InputValidator.readNonEmptyString("Thoi gian bat dau : ");
        String end   = InputValidator.readNonEmptyString("Thoi gian ket thuc: ");

        // BƯỚC 2: Chuẩn bị giỏ hàng thiết bị và lấy dữ liệu RAM
        List<BookingDetail> details = new ArrayList<>();
        List<Equipment> tempEqList = adminService.getAllEquipments();

        while (true) {
            System.out.print("\nBan co muon thue thiet bi? (1: Co | 0: Xac nhan dat phong): ");
            int choice = InputValidator.readInt("");
            if (choice == 0) break;

            System.out.println("\n--- KHO THIET BI (CAP NHAT THEO GIO HANG) ---");
            System.out.printf("%-5s | %-18s | %-10s\n", "ID", "Ten thiet bi", "San co");
            System.out.println("-".repeat(45));

            for (Equipment e : tempEqList) {
                if (e.getAvailableQty() > 0) {
                    System.out.printf("%-5d | %-18s | %-10d\n",
                            e.getId(), e.getName(), e.getAvailableQty());
                }
            }
            System.out.println("-".repeat(45));

            int eqId = InputValidator.readInt("Nhap ID Thiet bi: ");
            int qty = InputValidator.readInt("So luong thue: ");

            boolean exists = false;
            for (Equipment e : tempEqList) {
                if (e.getId() == eqId) {
                    exists = true;
                    if (qty <= 0) {
                        System.out.println("[LOI] So luong phai lon hon 0!");
                    } else if (qty > e.getAvailableQty()) {
                        System.out.println("[LOI] Khong du! Kho chi con: " + e.getAvailableQty());
                    } else {
                        // TRỪ TRỰC TIẾP TRÊN RAM ĐỂ VÒNG LẶP SAU HIỆN SỐ MỚI
                        e.setAvailableQty(e.getAvailableQty() - qty);

                        // Thêm vào danh sách chi tiết (details)
                        details.add(new BookingDetail(eqId, 0, qty));
                        System.out.println("[OK] Da them " + qty + " " + e.getName() + " vao gio hang.");
                    }
                    break;
                }
            }
            if (!exists) System.out.println("[LOI] ID thiet bi khong hop le!");
        }

        // BƯỚC 3: Gửi yêu cầu sang Service
        System.out.println("\nDang kiem tra dieu kien va luu he thong...");

        Booking b = new Booking();
        b.setUserId(currentUser.getId());
        b.setRoomId(rId);
        b.setStartTime(start);
        b.setEndTime(end);

        // Service sẽ check: Thời gian quá khứ, Trùng lịch, Sức chứa
        String result = bookingService.executeBooking(b, details, participants);

        System.out.println("\n" + "=".repeat(45));
        System.out.println("===> KET QUA: " + result);
        System.out.println("=".repeat(45));

        // Log hoạt động
        util.Logger.log("User " + currentUser.getUsername() + " gui yeu cau dat phong ID " + rId);
    }

    private void renderMyHistory() {
        System.out.println("\n--- LICH SU DAT PHONG CUA BAN ---");
        List<Booking> list = bookingService.getHistoryByUserId(currentUser.getId());
        System.out.printf("%-5s | %-10s | %-15s | %-10s\n", "ID", "Phong", "Trang thai", "Chuan bi");
        for (Booking b : list) {
            System.out.printf("%-5d | %-10d | %-15s | %-10s\n", b.getId(), b.getRoomId(), b.getStatus(), b.getPrepStatus());
        }
    }

    // ================== MENU SUPPORT ==================
    private void showSupportMenu() {
        while (true) {
            header("MENU HO TRO (SUPPORT)");
            line("  [1]", "Cong viec duoc phan cong");
            line("  [2]", "Cap nhat trang thai Ready");
            line("  [0]", "Dang xuat");
            footer();

            int c = readInt("");
            if (c == 0) { currentUser = null; break; }
            switch (c) {
                case 1: renderAssignedTasks(); break;
                case 2: handleUpdatePrepStatus(); break;
            }
        }
    }

    private void renderAssignedTasks() {
        System.out.println("\n--- CONG VIEC DUOC PHAN CONG ---");
        List<Booking> tasks = bookingService.getTasksBySupportId(currentUser.getId());
        for (Booking t : tasks) {
            System.out.printf("Booking ID: %d | Phong: %d | Time: %s\n", t.getId(), t.getRoomId(), t.getStartTime());
        }
    }

    private void handleUpdatePrepStatus() {
        header("CAP NHAT TRANG THAI CHUAN BI");

        // 1. Tự động hiện bảng danh sách công việc trước để Staff dễ nhìn ID
        List<Booking> tasks = bookingService.getTasksBySupportId(currentUser.getId());

        if (tasks.isEmpty()) {
            System.out.println("Ban khong co cong viec nao de cap nhat!");
            return;
        }

        System.out.println("\n--- DANH SACH CONG VIEC CUA BAN ---");
        System.out.printf("%-5s | %-10s | %-15s | %-15s\n", "ID", "Phong", "Thoi gian", "Hien tai");
        System.out.println("-------------------------------------------------------------");
        for (Booking t : tasks) {
            System.out.printf("%-5d | %-10d | %-15s | %-15s\n",
                    t.getId(), t.getRoomId(), t.getStartTime(), t.getPrepStatus());
        }
        System.out.println("-------------------------------------------------------------");

        // 2. Sau khi hiện bảng mới yêu cầu nhập ID
        int bId = readInt("\n==> Nhap ID Booking ban muon thay doi: ");

        // Kiểm tra xem ID nhập vào có nằm trong danh sách tasks của Staff này không
        boolean isValidId = tasks.stream().anyMatch(t -> t.getId() == bId);
        if (!isValidId) {
            System.out.println("ID khong hop le hoac khong thuoc quyen quan ly cua ban!");
            return;
        }

        // 3. Chọn trạng thái mới
        System.out.println("\nChon trang thai moi:");
        System.out.println("[1] READY (Phong da san sang)");
        System.out.println("[2] MISSING (Thieu thiet bi/Su co)");
        System.out.println("[3] PREPARING (Dang sap xep)");

        int choice = readInt("Chon (1-3): ");
        String status = (choice == 1) ? "READY" : (choice == 2) ? "MISSING" : "PREPARING";

        // 4. Gọi Service cập nhật
        if (bookingService.updatePrepStatus(bId, status)) {
            System.out.println("Cap nhat thanh cong! Trang thai moi: " + status);
        } else {
            System.out.println("Co loi xay ra khi luu du lieu.");
        }
    }

    // ================== CRUD ROOM & EQUIPMENT ==================
    private void renderRoomManagement() {
        while (true) {
            List<Room> rooms = adminService.getAllRooms();
            System.out.println("\n          =========== DANH SACH PHONG ===========");
            System.out.printf("%-3s | %-15s | %-8s | %-10s | %-10s\n", "ID", "Ten phong", "Suc chua", "Vi tri", "Trang thai");
            for (Room r : rooms) {
                System.out.printf("%-3d | %-15s | %-8d | %-10s | %-10s\n", r.getId(), r.getRoomName(), r.getCapacity(), r.getLocation(), (r.getStatus() == 1 ? "Hoat dong" : "Bao tri"));
            }
            header("QUAN LY PHONG");
            line("  [1]", "Them phong"); line("  [2]", "Sua phong"); line("  [3]", "Xoa phong"); line("  [0]", "Quay lai");
            footer();
            int sub = readInt("");
            if (sub == 0) break;
            if (sub == 1) {
                String name = readString("Ten: ");
                int cap = readInt("Suc chua: ");
                String loc = readString("Vi tri: ");
                adminService.addRoom(new Room(0, name, cap, loc, 1));
            } else if (sub == 2) {
                int id = readInt("ID: ");
                String name = readString("Ten moi: ");
                int cap = readInt("Suc chua: ");
                String loc = readString("Vi tri: ");
                int st = readInt("Trang thai (1/0): ");
                adminService.updateRoom(new Room(id, name, cap, loc, st));
            } else if (sub == 3) {
                int idXoa = readInt("ID muon xoa: ");
                String confirm = readString("Ban co chac chan muon xoa ID " + idXoa + "? (y/n): ");
                if (confirm.equalsIgnoreCase("y")) {
                    adminService.deleteRoom(idXoa);
                    System.out.println("Da xoa!");
                }
            }
        }
    }

    private void handleEquipmentManager() {
        while (true) {
            List<Equipment> list = adminService.getAllEquipments();
            System.out.println("\n      =========== THIET BI ===========");
            System.out.printf("%-3s | %-12s | %-5s | %-6s\n", "ID", "Ten", "Tong", "San co");
            for (Equipment e : list) {
                System.out.printf("%-3d | %-12s | %-5d | %-6d\n", e.getId(), e.getName(), e.getTotalQty(), e.getAvailableQty());
            }
            header("THIET BI");
            line("  [1]", "Them"); line("  [2]", "Cap nhat"); line("  [0]", "Quay lai");
            footer();
            int c = readInt("");
            if (c == 0) break;
            if (c == 1) {
                String name = readString("Ten: ");
                int q = readInt("So luong: ");
                adminService.addEquipment(new Equipment(0, name, q, q));
            } else if (c == 2) {
                int id = readInt("ID: ");
                int t = readInt("Tong: ");
                int a = readInt("San co: ");
                adminService.updateEquipment(id, t, a);
            }
        }
    }

    private void handleCreateStaff() {
        simpleHeader("TAO TAI KHOAN STAFF");
        String u = readString("Username: ");
        String p = readString("Password: ");
        String name = readString("Ho ten: ");
        String role = readString("Role (ADMIN/SUPPORT): ");
        User staff = new User();
        staff.setUsername(u); staff.setFullName(name); staff.setRole(role);
        System.out.println(adminService.createStaffAccount(staff, p));
    }

    private void renderAvailableRooms() {
        // Gọi qua adminService hoặc trực tiếp resourceDAO tùy cấu trúc của bạn
        List<Room> rooms = adminService.getAllRooms();

        System.out.println("\n" + "=".repeat(75));
        System.out.println("                DANH SACH PHONG CO THE DAT");
        System.out.println("=".repeat(75));

        // Thêm cột %-10s cho Sức chứa
        System.out.printf("%-5s | %-20s | %-10s | %-15s | %-10s\n",
                "ID", "Ten phong", "Suc chua", "Vi tri", "Trang thai");
        System.out.println("-".repeat(75));

        for (Room r : rooms) {
            String statusText = (r.getStatus() == 1) ? "Hoat dong" : "Bao tri";
            // Chú ý: Trong DAO của bạn status là INT (1: Active, 0: Maintenance)
            if (r.getStatus() == 1) {
                System.out.printf("%-5d | %-20s | %-10d | %-15s | %-10s\n",
                        r.getId(),
                        r.getRoomName(),
                        r.getCapacity(), // Lấy từ ResourceDAO đã có
                        r.getLocation(),
                        statusText);
            }
        }
        System.out.println("=".repeat(75));
    }
    private void handleSearchRoom() {
        String keyword = InputValidator.readNonEmptyString("Nhap ten phong muon tim: ");
        List<Room> results = adminService.searchRooms(keyword); // Bạn cần gọi qua Service
        if (results.isEmpty()) {
            System.out.println("(!) Khong tim thay phong nao phu hop.");
        } else {
            System.out.printf("%-5s | %-15s | %-10s\n", "ID", "Ten Phong", "Suc Chua");
            for (Room r : results) {
                System.out.printf("%-5d | %-15s | %-10d\n", r.getId(), r.getRoomName(), r.getCapacity());
            }
        }
    }
    private void handleCancelBooking() {
        renderMyHistory(); // Hien thi lich su de user xem ID
        int id = InputValidator.readInt("Nhap ID Booking muon huy: ");
        if (bookingService.cancelBooking(id, currentUser.getId())) {
            System.out.println("[OK] Da huy yeu cau dat phong thanh cong.");
            util.Logger.log("User " + currentUser.getUsername() + " da huy booking ID: " + id);
        } else {
            System.out.println("[LOI] Khong the huy (Co the do ID sai hoac phong da duoc duyet).");
        }
    }
    private void handleUpdateProfile() {
        header("CAP NHAT HO SO CA NHAN");
        System.out.println("Ten hien tai: " + currentUser.getFullName());
        String newName = readString("Nhap ten moi (De trong neu khong doi): ");

        if (!newName.isEmpty()) {
            currentUser.setFullName(newName);
            // Bạn cần gọi authService.updateProfile(currentUser)
            // Đảm bảo AuthService đã có hàm updateProfile
            System.out.println("Cap nhat thanh cong!");
        }
    }

}