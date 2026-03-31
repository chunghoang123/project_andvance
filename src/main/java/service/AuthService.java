package service;

import dao.UserDAO;
import model.User;
import util.PasswordHash;

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    // Logic Đăng ký
    public String register(User user, String plainPassword) {
        // 1. Hash mật khẩu trước khi gửi sang DAO (Yêu cầu 6.3)
        String hashed = PasswordHash.hashPassword(plainPassword);
        user.setPassword(hashed);

        if (userDAO.register(user)) {
            return "Dang ky thanh cong!";
        }
        return "Dang ky that bai (Username da ton tai).";
    }

    // Logic Đăng nhập
    public User login(String username, String plainPassword) {
        User user = userDAO.getUserByUsername(username);

        if (user != null) {
            // So khớp BCrypt (Yêu cầu 6.3)
            if (PasswordHash.checkPassword(plainPassword, user.getPassword())) {
                return user; // Đăng nhập thành công
            }
        }
        return null; // Sai user hoặc pass
    }
}