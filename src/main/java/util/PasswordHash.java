package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHash {
    // Hàm băm mật khẩu
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Hàm kiểm tra mật khẩu
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            // hashedPassword phải bắt đầu bằng $2a$, $2b$ hoặc $2y$
            if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
                return false;
            }
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Loi format mat khau trong DB: " + e.getMessage());
            return false;
        }
    }
}