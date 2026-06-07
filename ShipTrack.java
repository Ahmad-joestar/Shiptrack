package shipTrack;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ShipTrack {

  
    public static void main(String[] args) {
        try (Scanner scan = new Scanner(System.in)) {
            UserStore.initAdminIfNeeded();

            System.out.println("       Welcome to ShipTrack System       ");

            boolean running = true;
            while (running) {
                System.out.println("\nMain Menu:");
                System.out.println("1. Login");
                System.out.println("2. Register as customer");
                System.out.println("3. Exit");
                System.out.println("Choose an option(Write Its number):");
                int choice;

                try {
                    choice = scan.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scan.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1:
                        String[] user = AuthService.login(scan);
                        if (user != null) {
                            String role = user[2];
                            if ("admin".equalsIgnoreCase(role)) {
                                AdminMenu.menu(scan, user);
                            } else if ("dispatcher".equalsIgnoreCase(role)) {
                                DispatcherMenu.menu(scan, user);
                            } else if ("driver".equalsIgnoreCase(role)) {
                                DriverMenu.menu(scan, user);
                            } else if ("customer".equalsIgnoreCase(role)) {
                                CustomerMenu.scan(scan, user);
                            } else {
                                System.out.println("Unknown role. Failed Access.");
                                MyLogger.writeToLog("Unknown role login attempted: " + user[0]);
                            }
                        }
                        break;
                    case 2:
                        CustomerMenu.register(scan);
                        break;
                    case 3:
                        running = false;
                        System.out.println("Session finished");
                        break;
                    default:
                        System.out.println("Invalid option. please try again.");
                }
            }
        }
    }
}