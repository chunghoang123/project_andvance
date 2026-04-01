package service;

import dao.UserDAO;
import model.User;
import util.PasswordHash;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public String register(User user, String plainPassword) {
        if (user == null) return "[LOI] Du lieu nguoi dung khong hop le!";

        String username = safe(user.getUsername());
        String fullName = safe(user.getFullName());
        String role = safe(user.getRole()).isEmpty() ? "EMPLOYEE" : safe(user.getRole()).toUpperCase();

        if (username.isEmpty()) return "[LOI] Username khong duoc de trong!";
        if (fullName.isEmpty()) return "[LOI] Ho ten khong duoc de trong!";
        if (plainPassword == null || plainPassword.trim().length() < 6) {
            return "[LOI] Mat khau phai co it nhat 6 ky tu!";
        }
        if (userDAO.usernameExists(username)) {
            return "[LOI] Username da ton tai!";
        }

        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPassword(PasswordHash.hashPassword(plainPassword.trim()));

        return userDAO.register(user)
                ? "[OK] Dang ky thanh cong!"
                : "[LOI] Dang ky that bai!";
    }

    public User login(String username, String plainPassword) {
        if (username == null || plainPassword == null) return null;

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) return null;

        return PasswordHash.checkPassword(plainPassword, user.getPassword()) ? user : null;
    }

    public boolean updateProfile(User user) {
        if (user == null || user.getId() <= 0) return false;
        if (safe(user.getFullName()).isEmpty()) return false;
        return userDAO.updateProfile(user);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
