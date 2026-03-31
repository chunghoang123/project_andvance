package presentation;

import util.DataInitializer;

public class MainApp {

    public static void printHeader() {
        System.out.println("|==================================================|");
        System.out.println("|          PHAN MEM QUAN LY PHONG HOP PRJ-05       |");
        System.out.println("|==================================================|");
    }

    public static void loading() {
        System.out.print("Dang khoi dong he thong");
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(400);
                System.out.print(".");
            }
        } catch (InterruptedException e) {
            System.out.println(" Loi loading");
        }
        System.out.println("\n");
    }

    public static void main(String[] args) {
        MenuConsole menu = new MenuConsole();
        DataInitializer.initAdmin();

        printHeader();
        loading();

        try {
            menu.start();
        } catch (Exception e) {
            System.out.println("He thong xay ra loi: " + e.getMessage());
        } finally {
            System.out.println("\n|========================================|");
            System.out.println("|              TAM BIET                  |");
            System.out.println("|========================================|");
        }
    }
}