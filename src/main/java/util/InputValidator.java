package util;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Scanner sc = new Scanner(System.in);

    // 1. Kiem tra so nguyen duong (Dung cho Capacity, Quantity)
    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(sc.nextLine());
                if (value >= 0) return value;
                System.out.println("Loi: Gia tri phai la so duong!");
            } catch (NumberFormatException e) {
                System.out.println("Loi: Vui long nhap so nguyen hop le!");
            }
        }
    }

    // 2. Kiem tra chuoi khong duoc de trong (Dung cho Username, RoomName)
    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Loi: Khong duoc de trong thong tin nay!");
        }
    }

    // 3. Kiem tra dinh dang So dien thoai (10 chu so)
    public static String readPhone(String prompt) {
        String regex = "^\\d{10}$";
        while (true) {
            System.out.print(prompt);
            String phone = sc.nextLine().trim();
            if (Pattern.matches(regex, phone)) return phone;
            System.out.println("Loi: So dien thoai phai co dung 10 chu so!");
        }
    }

    // 4. Kiem tra Password (It nhat 6 ky tu)
    public static String readPassword(String prompt) {
        while (true) {
            System.out.print(prompt);
            String pass = sc.nextLine().trim();
            if (pass.length() >= 6) return pass;
            System.out.println("Loi: Mat khau phai co it nhat 6 ky tu!");
        }
    }
}