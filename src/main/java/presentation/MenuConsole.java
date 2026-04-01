package presentation;

import model.Booking;
import model.BookingDetail;
import model.Equipment;
import model.Room;
import model.User;
import service.AdminService;
import service.AuthService;
import service.BookingService;
import service.FeedbackService;
import service.ReportService;
import util.Logger;
import util.NotificationService;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MenuConsole {

    private final Scanner sc = new Scanner(System.in);

    private final AuthService authService = new AuthService();
    private final AdminService adminService = new AdminService();
    private final BookingService bookingService = new BookingService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final ReportService reportService = new ReportService();

    private User currentUser = null;

    // ========================= UI HELPERS =========================
    private void printLine() {
        System.out.println("=".repeat(80));
    }

    private void printTitle(String title) {
        System.out.println();
        printLine();
        System.out.printf("%s%n", centerText(title, 80));
        printLine();
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        return " ".repeat(leftPadding) + text;
    }

    private void pause() {
        System.out.print("\nNhan ENTER de tiep tuc...");
        sc.nextLine();
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (!input.isEmpty()) return input;
            System.out.println("[LOI] Khong duoc de trong!");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("[LOI] Vui long nhap so nguyen hop le!");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("[LOI] Gia tri phai > 0!");
        }
    }

    private String readPhone(String prompt, boolean allowBlank) {
        String regex = "^\\d{10}$";
        while (true) {
            String input = readString(prompt);
            if (allowBlank && input.isEmpty()) return "";
            if (Pattern.matches(regex, input)) return input;
            System.out.println("[LOI] So dien thoai phai gom dung 10 chu so!");
        }
    }

    private String readPasswordMasked(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return pwd == null ? "" : new String(pwd).trim();
        }

        // Fallback khi chay trong IDE khong ho tro Console
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private String chooseRole() {
        while (true) {
            String role = readNonEmpty("Role (ADMIN/SUPPORT): ").toUpperCase();
            if (role.equals("ADMIN") || role.equals("SUPPORT")) return role;
            System.out.println("[LOI] Role chi duoc la ADMIN hoac SUPPORT!");
        }
    }

    private String choosePrepStatus() {
        while (true) {
            System.out.println("[1] PREPARING");
            System.out.println("[2] READY");
            System.out.println("[3] MISSING");
            int choice = readInt("Chon trang thai: ");
            switch (choice) {
                case 1: return "PREPARING";
                case 2: return "READY";
                case 3: return "MISSING";
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    // ========================= MAIN FLOW =========================
    public void start() {
        while (true) {
            printTitle("HE THONG QUAN LY DAT PHONG HOP");
            System.out.println("[1] Dang nhap");
            System.out.println("[2] Dang ky tai khoan Employee");
            System.out.println("[0] Thoat");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 0:
                    System.out.println("Tam biet!");
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void handleLogin() {
        printTitle("DANG NHAP");
        String username = readNonEmpty("Username: ");
        String password = readPasswordMasked("Password: ");

        currentUser = authService.login(username, password);

        if (currentUser == null) {
            System.out.println("[LOI] Sai username hoac password!");
            pause();
            return;
        }

        System.out.println("[OK] Dang nhap thanh cong! Xin chao " + currentUser.getFullName());

        String role = currentUser.getRole() == null ? "" : currentUser.getRole().toUpperCase();
        switch (role) {
            case "ADMIN":
                showAdminMenu();
                break;
            case "EMPLOYEE":
                showEmployeeMenu();
                break;
            case "SUPPORT":
                showSupportMenu();
                break;
            default:
                System.out.println("[LOI] Role khong hop le trong he thong!");
                currentUser = null;
                pause();
        }
    }

    private void handleRegister() {
        printTitle("DANG KY TAI KHOAN EMPLOYEE");

        User user = new User();
        user.setUsername(readNonEmpty("Username: "));
        String password = readPasswordMasked("Password (toi thieu 6 ky tu): ");
        user.setFullName(readNonEmpty("Ho ten: "));
        user.setDepartment(readString("Phong ban (co the bo trong): "));
        user.setPhone(readPhone("So dien thoai 10 chu so (co the bo trong): ", true));
        user.setRole("EMPLOYEE");

        String result = authService.register(user, password);
        System.out.println(result);
        pause();
    }

    // ========================= ADMIN MENU =========================
    private void showAdminMenu() {
        while (true) {
            printTitle("MENU QUAN TRI - ADMIN");
            System.out.println("[1] Quan ly phong hop");
            System.out.println("[2] Quan ly thiet bi");
            System.out.println("[3] Danh sach nguoi dung");
            System.out.println("[4] Tao tai khoan Support/Admin");
            System.out.println("[5] Duyet / Tu choi booking");
            System.out.println("[6] Bao cao / Phan tich");
            System.out.println("[0] Dang xuat");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    handleRoomManagement();
                    break;
                case 2:
                    handleEquipmentManagement();
                    break;
                case 3:
                    renderAllUsers();
                    break;
                case 4:
                    handleCreateStaff();
                    break;
                case 5:
                    handleAdminApproval();
                    break;
                case 6:
                    handleSystemReports();
                    break;
                case 0:
                    currentUser = null;
                    System.out.println("Da dang xuat!");
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void renderAllUsers() {
        printTitle("DANH SACH NGUOI DUNG");

        List<User> users = adminService.getAllUsers();
        if (users == null || users.isEmpty()) {
            System.out.println("Khong co nguoi dung nao.");
            pause();
            return;
        }

        System.out.printf("%-5s | %-20s | %-25s | %-12s | %-15s | %-12s%n",
                "ID", "Username", "Ho ten", "Role", "Phong ban", "Phone");
        printLine();

        for (User u : users) {
            System.out.printf("%-5d | %-20s | %-25s | %-12s | %-15s | %-12s%n",
                    u.getId(),
                    safe(u.getUsername()),
                    safe(u.getFullName()),
                    safe(u.getRole()),
                    safe(u.getDepartment()),
                    safe(u.getPhone()));
        }

        pause();
    }

    private void handleCreateStaff() {
        printTitle("TAO TAI KHOAN SUPPORT / ADMIN");

        User user = new User();
        user.setUsername(readNonEmpty("Username: "));
        String password = readPasswordMasked("Password (toi thieu 6 ky tu): ");
        user.setFullName(readNonEmpty("Ho ten: "));
        user.setDepartment(readString("Phong ban (co the bo trong): "));
        user.setPhone(readPhone("So dien thoai 10 chu so (co the bo trong): ", true));
        user.setRole(chooseRole());

        String result = adminService.createStaffAccount(user, password);
        System.out.println(result);
        pause();
    }

    private void handleRoomManagement() {
        while (true) {
            printTitle("QUAN LY PHONG HOP");
            renderRoomTable(adminService.getAllRooms());

            System.out.println("\n[1] Them phong");
            System.out.println("[2] Sua phong");
            System.out.println("[3] Xoa phong");
            System.out.println("[4] Tim phong theo ten");
            System.out.println("[0] Quay lai");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    handleAddRoom();
                    break;
                case 2:
                    handleUpdateRoom();
                    break;
                case 3:
                    handleDeleteRoom();
                    break;
                case 4:
                    handleSearchRoom();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void handleAddRoom() {
        printTitle("THEM PHONG MOI");

        String name = readNonEmpty("Ten phong: ");
        int capacity = readPositiveInt("Suc chua: ");
        String location = readNonEmpty("Vi tri: ");
        int status = 1;

        Room room = new Room(0, name, capacity, location, status);
        String result = adminService.addRoom(room);
        System.out.println(result);
        pause();
    }

    private void handleUpdateRoom() {
        printTitle("SUA THONG TIN PHONG");

        int id = readPositiveInt("Nhap ID phong can sua: ");
        Room oldRoom = adminService.getRoomById(id);

        if (oldRoom == null) {
            System.out.println("[LOI] Khong tim thay phong!");
            pause();
            return;
        }

        System.out.println("Thong tin cu:");
        System.out.println("- Ten phong : " + oldRoom.getRoomName());
        System.out.println("- Suc chua  : " + oldRoom.getCapacity());
        System.out.println("- Vi tri    : " + oldRoom.getLocation());
        System.out.println("- Trang thai: " + (oldRoom.getStatus() == 1 ? "Hoat dong" : "Bao tri"));

        String name = readString("Ten moi (ENTER de giu nguyen): ");
        String capStr = readString("Suc chua moi (ENTER de giu nguyen): ");
        String location = readString("Vi tri moi (ENTER de giu nguyen): ");
        String statusStr = readString("Trang thai moi 1=Hoat dong, 0=Bao tri (ENTER de giu nguyen): ");

        Room updated = new Room();
        updated.setId(oldRoom.getId());
        updated.setRoomName(name.isEmpty() ? oldRoom.getRoomName() : name);
        updated.setCapacity(capStr.isEmpty() ? oldRoom.getCapacity() : Integer.parseInt(capStr));
        updated.setLocation(location.isEmpty() ? oldRoom.getLocation() : location);
        updated.setStatus(statusStr.isEmpty() ? oldRoom.getStatus() : Integer.parseInt(statusStr));

        String result = adminService.updateRoom(updated);
        System.out.println(result);
        pause();
    }

    private void handleDeleteRoom() {
        printTitle("XOA PHONG");
        int id = readPositiveInt("Nhap ID phong can xoa: ");
        String confirm = readNonEmpty("Ban co chac chan muon xoa? (y/n): ");

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Da huy thao tac xoa.");
            pause();
            return;
        }

        String result = adminService.deleteRoom(id);
        System.out.println(result);
        pause();
    }

    private void handleSearchRoom() {
        printTitle("TIM KIEM PHONG THEO TEN");
        String keyword = readNonEmpty("Nhap ten phong can tim: ");

        List<Room> results = adminService.searchRooms(keyword);
        if (results == null || results.isEmpty()) {
            System.out.println("Khong tim thay phong phu hop.");
        } else {
            renderRoomTable(results);
        }

        pause();
    }

    private void renderRoomTable(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            System.out.println("Khong co du lieu phong.");
            return;
        }

        System.out.printf("%-5s | %-22s | %-10s | %-20s | %-12s%n",
                "ID", "Ten phong", "Suc chua", "Vi tri", "Trang thai");
        printLine();

        for (Room r : rooms) {
            System.out.printf("%-5d | %-22s | %-10d | %-20s | %-12s%n",
                    r.getId(),
                    safe(r.getRoomName()),
                    r.getCapacity(),
                    safe(r.getLocation()),
                    r.getStatus() == 1 ? "Hoat dong" : "Bao tri");
        }
    }

    private void handleEquipmentManagement() {
        while (true) {
            printTitle("QUAN LY THIET BI");
            renderEquipmentTable(adminService.getAllEquipments());

            System.out.println("\n[1] Them thiet bi");
            System.out.println("[2] Sua thiet bi");
            System.out.println("[3] Xoa thiet bi");
            System.out.println("[0] Quay lai");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    handleAddEquipment();
                    break;
                case 2:
                    handleUpdateEquipment();
                    break;
                case 3:
                    handleDeleteEquipment();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void handleAddEquipment() {
        printTitle("THEM THIET BI");
        String name = readNonEmpty("Ten thiet bi: ");
        int total = readPositiveInt("Tong so luong: ");
        int available = readPositiveInt("So luong san co: ");

        Equipment eq = new Equipment(0, name, total, available);
        String result = adminService.addEquipment(eq);
        System.out.println(result);
        pause();
    }

    private void handleUpdateEquipment() {
        printTitle("SUA THIET BI");

        int id = readPositiveInt("Nhap ID thiet bi can sua: ");
        Equipment oldEq = adminService.getEquipmentById(id);

        if (oldEq == null) {
            System.out.println("[LOI] Khong tim thay thiet bi!");
            pause();
            return;
        }

        System.out.println("Thong tin cu:");
        System.out.println("- Ten        : " + oldEq.getName());
        System.out.println("- Tong SL    : " + oldEq.getTotalQty());
        System.out.println("- San co     : " + oldEq.getAvailableQty());

        String name = readString("Ten moi (ENTER de giu nguyen): ");
        String totalStr = readString("Tong SL moi (ENTER de giu nguyen): ");
        String availStr = readString("SL san co moi (ENTER de giu nguyen): ");

        Equipment updated = new Equipment();
        updated.setId(oldEq.getId());
        updated.setName(name.isEmpty() ? oldEq.getName() : name);
        updated.setTotalQty(totalStr.isEmpty() ? oldEq.getTotalQty() : Integer.parseInt(totalStr));
        updated.setAvailableQty(availStr.isEmpty() ? oldEq.getAvailableQty() : Integer.parseInt(availStr));

        String result = adminService.updateEquipment(updated);
        System.out.println(result);
        pause();
    }

    private void handleDeleteEquipment() {
        printTitle("XOA THIET BI");
        int id = readPositiveInt("Nhap ID thiet bi can xoa: ");
        String confirm = readNonEmpty("Ban co chac chan muon xoa? (y/n): ");

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Da huy thao tac xoa.");
            pause();
            return;
        }

        String result = adminService.deleteEquipment(id);
        System.out.println(result);
        pause();
    }

    private void renderEquipmentTable(List<Equipment> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("Khong co du lieu thiet bi.");
            return;
        }

        System.out.printf("%-5s | %-25s | %-12s | %-12s%n",
                "ID", "Ten thiet bi", "Tong SL", "San co");
        printLine();

        for (Equipment e : list) {
            System.out.printf("%-5d | %-25s | %-12d | %-12d%n",
                    e.getId(),
                    safe(e.getName()),
                    e.getTotalQty(),
                    e.getAvailableQty());
        }
    }

    private void handleAdminApproval() {
        printTitle("DUYET / TU CHOI BOOKING");

        List<Booking> pendings = bookingService.getAllPending();
        if (pendings == null || pendings.isEmpty()) {
            System.out.println("Khong co booking PENDING nao.");
            pause();
            return;
        }

        renderPendingBookings(pendings);

        int bookingId = readPositiveInt("Nhap ID booking can xu ly: ");
        System.out.println("[1] Duyet va phan cong Support");
        System.out.println("[2] Tu choi booking");
        System.out.println("[0] Huy");
        int action = readInt("Lua chon: ");

        switch (action) {
            case 1:
                List<User> supports = adminService.getAllSupportStaff();
                if (supports == null || supports.isEmpty()) {
                    System.out.println("[LOI] He thong chua co tai khoan SUPPORT nao!");
                    pause();
                    return;
                }

                System.out.printf("%-5s | %-20s | %-25s | %-12s%n", "ID", "Username", "Ho ten", "Role");
                printLine();
                for (User s : supports) {
                    System.out.printf("%-5d | %-20s | %-25s | %-12s%n",
                            s.getId(),
                            safe(s.getUsername()),
                            safe(s.getFullName()),
                            safe(s.getRole()));
                }

                int supportId = readPositiveInt("Nhap ID staff support: ");
                String approveResult = adminService.approveBooking(bookingId, supportId);
                System.out.println(approveResult);
                break;

            case 2:
                String rejectResult = adminService.rejectBooking(bookingId);
                System.out.println(rejectResult);
                break;

            case 0:
                System.out.println("Da huy thao tac.");
                break;

            default:
                System.out.println("[LOI] Lua chon khong hop le!");
        }

        pause();
    }

    private void renderPendingBookings(List<Booking> pendings) {
        System.out.printf("%-5s | %-8s | %-8s | %-18s | %-18s | %-12s%n",
                "ID", "UserID", "RoomID", "Start", "End", "Status");
        printLine();

        for (Booking b : pendings) {
            System.out.printf("%-5d | %-8d | %-8d | %-18s | %-18s | %-12s%n",
                    b.getId(),
                    b.getUserId(),
                    b.getRoomId(),
                    safe(b.getStartTime()),
                    safe(b.getEndTime()),
                    safe(b.getStatus()));
        }
    }

    private void handleSystemReports() {
        while (true) {
            printTitle("BAO CAO / PHAN TICH");
            System.out.println("[1] Phan tich tan suat su dung phong");
            System.out.println("[2] Xuat bao cao chi phi booking");
            System.out.println("[0] Quay lai");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    reportService.analyzeUsage();
                    pause();
                    break;
                case 2:
                    int bookingId = readPositiveInt("Nhap ID booking: ");
                    reportService.exportCostReport(bookingId);
                    pause();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    // ========================= EMPLOYEE MENU =========================
    private void showEmployeeMenu() {
        while (true) {
            printTitle("MENU NHAN VIEN - EMPLOYEE");
            System.out.println("[1] Xem danh sach phong hop");
            System.out.println("[2] Tim kiem phong theo ten");
            System.out.println("[3] Dat phong moi");
            System.out.println("[4] Xem lich su booking cua toi");
            System.out.println("[5] Huy booking PENDING");
            System.out.println("[6] Cap nhat ho so ca nhan");
            System.out.println("[7] Gui feedback sau cuoc hop");
            System.out.println("[0] Dang xuat");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    renderAvailableRooms();
                    pause();
                    break;
                case 2:
                    handleSearchRoom();
                    break;
                case 3:
                    handleBookingRequest();
                    break;
                case 4:
                    renderMyHistory();
                    pause();
                    break;
                case 5:
                    handleCancelBooking();
                    break;
                case 6:
                    handleUpdateProfile();
                    break;
                case 7:
                    handleSendFeedback();
                    break;
                case 0:
                    currentUser = null;
                    System.out.println("Da dang xuat!");
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void renderAvailableRooms() {
        printTitle("DANH SACH PHONG HOP");
        List<Room> rooms = adminService.getAllRooms();

        if (rooms == null || rooms.isEmpty()) {
            System.out.println("Khong co phong nao trong he thong.");
            return;
        }

        System.out.printf("%-5s | %-22s | %-10s | %-20s | %-12s%n",
                "ID", "Ten phong", "Suc chua", "Vi tri", "Trang thai");
        printLine();

        for (Room r : rooms) {
            System.out.printf("%-5d | %-22s | %-10d | %-20s | %-12s%n",
                    r.getId(),
                    safe(r.getRoomName()),
                    r.getCapacity(),
                    safe(r.getLocation()),
                    r.getStatus() == 1 ? "Hoat dong" : "Bao tri");
        }
    }

    private void handleBookingRequest() {
        printTitle("DAT PHONG MOI");

        String start = readNonEmpty("Thoi gian bat dau (yyyy-MM-dd HH:mm): ");
        String end = readNonEmpty("Thoi gian ket thuc (yyyy-MM-dd HH:mm): ");

        if (bookingService.isUserConflicting(currentUser.getId(), start, end)) {
            System.out.println("[CANH BAO] Ban da co lich hop trung khung gio nay!");
            String confirm = readNonEmpty("Ban van muon tiep tuc? (y/n): ");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Da huy thao tac dat phong.");
                pause();
                return;
            }
        }

        List<Room> availableRooms = bookingService.searchAvailableRooms(start, end);
        if (availableRooms == null || availableRooms.isEmpty()) {
            System.out.println("[THONG BAO] Khong co phong trong trong khung gio nay.");
            pause();
            return;
        }

        System.out.println("\nDANH SACH PHONG TRONG:");
        renderRoomTable(availableRooms);

        int roomId = readPositiveInt("Nhap ID phong muon dat: ");
        int participants = readPositiveInt("Nhap so nguoi tham gia: ");

        List<BookingDetail> details = new ArrayList<>();
        List<Equipment> equipments = adminService.getAllEquipments();

        while (true) {
            String choose = readNonEmpty("Ban co muon them thiet bi? (y/n): ");
            if (!choose.equalsIgnoreCase("y")) break;

            if (equipments == null || equipments.isEmpty()) {
                System.out.println("Khong co thiet bi nao trong kho.");
                break;
            }

            System.out.printf("%-5s | %-25s | %-12s%n", "ID", "Ten thiet bi", "San co");
            printLine();
            for (Equipment e : equipments) {
                System.out.printf("%-5d | %-25s | %-12d%n",
                        e.getId(), safe(e.getName()), e.getAvailableQty());
            }

            int eqId = readPositiveInt("Nhap ID thiet bi: ");
            int qty = readPositiveInt("Nhap so luong: ");

            Equipment selected = null;
            for (Equipment e : equipments) {
                if (e.getId() == eqId) {
                    selected = e;
                    break;
                }
            }

            if (selected == null) {
                System.out.println("[LOI] ID thiet bi khong ton tai!");
                continue;
            }

            if (qty > selected.getAvailableQty()) {
                System.out.println("[LOI] So luong yeu cau vuot qua so luong san co!");
                continue;
            }

            details.add(new BookingDetail(eqId, 0, qty));
            System.out.println("[OK] Da them thiet bi vao yeu cau booking.");
        }

        Booking b = new Booking();
        b.setUserId(currentUser.getId());
        b.setRoomId(roomId);
        b.setStartTime(start);
        b.setEndTime(end);

        String result = bookingService.executeBooking(b, details, participants);
        System.out.println(result);

        if (result.startsWith("[OK]")) {
            NotificationService.send("Co booking moi tu user: " + currentUser.getUsername());
            Logger.log("User " + currentUser.getUsername() + " tao booking moi.");
        }

        pause();
    }

    private void renderMyHistory() {
        printTitle("LICH SU BOOKING CUA TOI");

        List<Booking> list = bookingService.getHistoryByUserId(currentUser.getId());
        if (list == null || list.isEmpty()) {
            System.out.println("Ban chua co booking nao.");
            return;
        }

        System.out.printf("%-5s | %-8s | %-18s | %-18s | %-12s | %-12s | %-10s%n",
                "ID", "RoomID", "Start", "End", "Status", "Prep", "Support");
        printLine();

        for (Booking b : list) {
            System.out.printf("%-5d | %-8d | %-18s | %-18s | %-12s | %-12s | %-10d%n",
                    b.getId(),
                    b.getRoomId(),
                    safe(b.getStartTime()),
                    safe(b.getEndTime()),
                    safe(b.getStatus()),
                    safe(b.getPrepStatus()),
                    b.getSupportId());
        }
    }

    private void handleCancelBooking() {
        renderMyHistory();
        int bookingId = readPositiveInt("Nhap ID booking muon huy: ");
        String confirm = readNonEmpty("Ban co chac chan muon huy booking nay? (y/n): ");

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Da huy thao tac.");
            pause();
            return;
        }

        boolean ok = bookingService.cancelBooking(bookingId, currentUser.getId());
        if (ok) {
            System.out.println("[OK] Huy booking thanh cong.");
            Logger.log("User " + currentUser.getUsername() + " huy booking ID " + bookingId);
        } else {
            System.out.println("[LOI] Khong the huy booking. Chi duoc huy booking o trang thai PENDING.");
        }

        pause();
    }

    private void handleUpdateProfile() {
        printTitle("CAP NHAT HO SO CA NHAN");

        System.out.println("Thong tin hien tai:");
        System.out.println("- Ho ten     : " + safe(currentUser.getFullName()));
        System.out.println("- Phong ban  : " + safe(currentUser.getDepartment()));
        System.out.println("- So dien thoai: " + safe(currentUser.getPhone()));

        String fullName = readString("Ho ten moi (ENTER de giu nguyen): ");
        String department = readString("Phong ban moi (ENTER de giu nguyen): ");
        String phone = readString("So dien thoai moi 10 chu so (ENTER de giu nguyen): ");

        User updated = new User();
        updated.setId(currentUser.getId());
        updated.setUsername(currentUser.getUsername());
        updated.setRole(currentUser.getRole());
        updated.setPassword(currentUser.getPassword());

        updated.setFullName(fullName.isEmpty() ? currentUser.getFullName() : fullName);
        updated.setDepartment(department.isEmpty() ? currentUser.getDepartment() : department);

        if (phone.isEmpty()) {
            updated.setPhone(currentUser.getPhone());
        } else {
            while (!Pattern.matches("^\\d{10}$", phone)) {
                System.out.println("[LOI] So dien thoai phai gom dung 10 chu so!");
                phone = readString("Nhap lai so dien thoai: ");
            }
            updated.setPhone(phone);
        }

        boolean ok = authService.updateProfile(updated);
        if (ok) {
            currentUser = updated;
            System.out.println("[OK] Cap nhat ho so thanh cong!");
        } else {
            System.out.println("[LOI] Cap nhat ho so that bai!");
        }

        pause();
    }

    private void handleSendFeedback() {
        printTitle("GUI FEEDBACK SAU CUOC HOP");
        renderMyHistory();

        int bookingId = readPositiveInt("Nhap ID booking muon danh gia: ");
        int rating;
        while (true) {
            rating = readInt("Nhap so sao (1-5): ");
            if (rating >= 1 && rating <= 5) break;
            System.out.println("[LOI] Rating phai trong khoang 1-5!");
        }

        String comment = readNonEmpty("Nhap noi dung gop y: ");
        boolean ok = feedbackService.sendFeedback(bookingId, rating, comment);

        if (ok) System.out.println("[OK] Gui feedback thanh cong!");
        else System.out.println("[LOI] Gui feedback that bai!");

        pause();
    }

    // ========================= SUPPORT MENU =========================
    private void showSupportMenu() {
        while (true) {
            printTitle("MENU NHAN VIEN HO TRO - SUPPORT");
            System.out.println("[1] Xem booking duoc phan cong");
            System.out.println("[2] Cap nhat trang thai chuan bi");
            System.out.println("[0] Dang xuat");
            printLine();

            int choice = readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    renderAssignedTasks();
                    pause();
                    break;
                case 2:
                    handleUpdatePrepStatus();
                    break;
                case 0:
                    currentUser = null;
                    System.out.println("Da dang xuat!");
                    return;
                default:
                    System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
    }

    private void renderAssignedTasks() {
        printTitle("DANH SACH BOOKING DUOC PHAN CONG");

        List<Booking> tasks = bookingService.getTasksBySupportId(currentUser.getId());
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("Ban chua duoc phan cong booking nao.");
            return;
        }

        System.out.printf("%-5s | %-8s | %-18s | %-18s | %-12s | %-12s%n",
                "ID", "RoomID", "Start", "End", "Status", "Prep");
        printLine();

        for (Booking b : tasks) {
            System.out.printf("%-5d | %-8d | %-18s | %-18s | %-12s | %-12s%n",
                    b.getId(),
                    b.getRoomId(),
                    safe(b.getStartTime()),
                    safe(b.getEndTime()),
                    safe(b.getStatus()),
                    safe(b.getPrepStatus()));
        }
    }

    private void handleUpdatePrepStatus() {
        printTitle("CAP NHAT TRANG THAI CHUAN BI");
        renderAssignedTasks();

        int bookingId = readPositiveInt("Nhap ID booking can cap nhat: ");
        String prepStatus = choosePrepStatus();

        boolean ok = bookingService.updatePrepStatus(bookingId, prepStatus);
        if (ok) {
            System.out.println("[OK] Cap nhat trang thai thanh cong!");
            Logger.log("Support " + currentUser.getUsername() + " cap nhat prep_status booking " + bookingId + " -> " + prepStatus);
        } else {
            System.out.println("[LOI] Cap nhat that bai!");
        }

        pause();
    }

    // ========================= COMMON =========================
    private String safe(String s) {
        return s == null ? "" : s;
    }
}
