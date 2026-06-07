import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class DriverMenu {

    private DriverMenu() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void menu(Scanner scan, String[] user) {
        MyLogger.writeToLog("Driver menu accessed by: " + user[0]);
        boolean running = true;
        while (running) {
            System.out.println("\n\t\t\tDelivery Person Menu");
            System.out.println("1. View assigned deliveries");
            System.out.println("2. Update status of delivery");
            System.out.println("3. View/Update personal information");
            System.out.println("4. Logout");
            System.out.println("Choose an option(write their respective number):");
            int choice;

            try {
                choice = scan.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                MyLogger.writeToLog("Invalid menu input by driver: " + user[0]);
                scan.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    viewAssignedDeliveries(user[0]);
                    break;
                case 2:
                    updateDeliveryStatus(scan, user[0]);
                    break;
                case 3:
                    updateInfo(scan, user);
                    break;
                case 4:
                    running = false;
                    System.out.println("Logged out.");
                    MyLogger.writeToLog("Driver logged out: " + user[0]);
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
                    
            }
        }
    }

    private static void viewAssignedDeliveries(String username) {
        System.out.println("\n\t\t\tYour Assigned Deliveries");
        MyLogger.writeToLog("Driver " + username + " is viewing assigned deliveries.");
        List<String[]> shipments = ShipmentStore.getShipmentsByDriver(username);
        if (shipments.isEmpty()) {
            System.out.println("No deliveries assigned.");
            return;
        }
        for (String[] s : shipments) {
            System.out.println("ID: " + s[0] + " | Customer: " + s[1] + " | From: " + s[3] + " | To: " + s[4]
                    + " | Status: " + s[5]);
        }
    }

    private static void updateDeliveryStatus(Scanner scan, String username) {
        viewAssignedDeliveries(username);
        System.out.println("\nEnter shipment ID to update:");
        String shipmentID = scan.next();
        MyLogger.writeToLog("Driver " + username + " attempting to update shipment: " + shipmentID);

        String[] shipment = ShipmentStore.findShipment(shipmentID);
        if (shipment == null || !shipment[2].equalsIgnoreCase(username)) {
            System.out.println("Shipment not found or not assigned to you.");
            MyLogger.writeToLog("Update status failed: shipment " + shipmentID + " not assigned to driver: " + username);
            return;
        }

        System.out.println("Choose new status:");
        System.out.println("1. picked up");
        System.out.println("2. in transit");
        System.out.println("3. delivered");
        int choice;

        try {
            choice = scan.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            MyLogger.writeToLog("Invalid status input by driver: " + username);
            scan.nextLine();
            return;
        }

        String newStatus = "";
        switch (choice) {
            case 1:
                newStatus = "picked up";
                break;
            case 2:
                newStatus = "in transit";
                break;
            case 3:
                newStatus = "delivered";
                break;
            default:
                System.out.println("Invalid choice.");
                MyLogger.writeToLog("Invalid status choice by driver: " + username);
                return;
        }

        ShipmentStore.updateStatus(shipmentID, newStatus);
        System.out.println("Status updated to: " + newStatus);
    }

    private static void updateInfo(Scanner scan, String[] user) {
        System.out.println("\n\t\t\tYour Information");
        MyLogger.writeToLog("Driver " + user[0] + " accessed personal info update.");
        System.out.println("Name: " + user[3]);
        System.out.println("ID Number: " + user[4]);
        System.out.println("Contact: " + user[5]);
        System.out.println("\nWhat would you like to update?");
        System.out.println("1. Name");
        System.out.println("2. Contact number");
        System.out.println("3. Password");
        System.out.println("4. Back");
        int choice;

        try {
            choice = scan.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            MyLogger.writeToLog("Invalid input in update info by driver: " + user[0]);
            scan.nextLine();
            return;
        }

        switch (choice) {
            case 1:
                System.out.println("Enter new name:");
                scan.nextLine();
                user[3] = scan.nextLine();
                UserStore.updateUser(user[0], user);
                System.out.println("Name updated.");
                MyLogger.writeToLog("Driver " + user[0] + " updated their name.");
                break;
            case 2:
                System.out.println("Enter new contact number:");
                user[5] = scan.next();
                UserStore.updateUser(user[0], user);
                System.out.println("Contact updated.");
                MyLogger.writeToLog("Driver " + user[0] + " updated their contact number.");
                break;
            case 3:
                System.out.println("Enter new password:");
                System.out.println(PasswordPolicy.getMinDigits() + " Digit\n" + PasswordPolicy.getMinLowercase() + " Lowercase character\n" + PasswordPolicy.getMinUppercase() + " Uppercase character\n" + PasswordPolicy.getMinSpecial() + " special character\n and at least have " + PasswordPolicy.getMinLength() + " character");
                String newPassword = scan.next();
                if (!PasswordPolicy.validate(newPassword)) {
                    System.out.println("Password does not meet requirements.");
                    MyLogger.writeToLog("Driver " + user[0] + " password change failed: policy not met.");
                    return;
                }
                user[1] = AuthService.getSHA256Hash(newPassword);
                UserStore.updateUser(user[0], user);
                System.out.println("Password updated.");
                MyLogger.writeToLog("Password changed for Driver: " + user[0]);
                break;
            case 4:
                MyLogger.writeToLog("Driver " + user[0] + " went back from update info.");
                break;
            default:
                System.out.println("Invalid choice");
               
        }
    }
}
