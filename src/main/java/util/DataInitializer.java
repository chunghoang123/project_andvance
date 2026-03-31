package util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DataInitializer {
    public static void initAdmin() {
        String user = "admin1";
        String pass = "123456";
        String hashed = PasswordHash.hashPassword(pass);

        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashed);
            ps.setString(2, user);
            ps.executeUpdate();
            System.out.println("Da tu dong cap nhat Hash moi cho Admin!");
        } catch (Exception e)
        {
            System.out.println("Loi");/* handle error */
        }
    }
}
