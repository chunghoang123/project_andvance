package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class MyDatabase {
    private static final String URL = "jdbc:mysql://localhost:3306/pri_meeting_java_3";
    private static final String USER = "root";
    private static final String PASSWORD = "Londeocan1";

    public static Connection getConnection(){
        try{
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        }catch (Exception e){
            System.out.println("Loi ket noi"+e.getMessage());
        }
        return null;
    }
}
