package util;


public class NotificationService {
    public static void send(String message) {
        // 1. In ra Console (Thông báo nhanh)
        System.out.println("\n[NOTIFICATION] " + message);

        // 2. Ghi vào file log (Lưu trữ)
        Logger.log(message);

        // 3. (Gợi ý) Gửi Email: Bạn có thể tích hợp JavaMail ở đây nếu cần.
    }
}