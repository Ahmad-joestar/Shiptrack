import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

   

    public static void menu(Scanner scan, String[] user) {
        
        boolean running = true;
        while (running) {
            System.out.println("\n\t\t\tAdmin Menu");
            System.out.println("1. Register new user (dispatcher or driver)");
            System.out.println("2. Remove dispatcher or delivery person");
            System.out.println("3. Make adjustments to password strength requirements");
            System.out.println("4. Set maximum login attempts");
            System.out.println("5. Lock or unlock an account");
            System.out.println("6. View users");
            System.out.println("7. Logout");
            System.out.println("Choose one option please (write its number):");
            int choice;

            try {
                choice = scan.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                MyLogger.writeToLog("Invalid menu input by admin: " + user[0]);
                scan.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    register(scan);
                    break;
                case 2:
                    removeUser(scan);
                    break;
                case 3:
                    setPasswordPolicy(scan);
                    break;
                case 4:
                    setMax(scan);
                    break;
                case 5:
                    lockOrUnlock(scan);
                    break;
                case 6:
                    viewAllUsers();
                    break;
                case 7:
                    running = false;
                    System.out.println("Logged out.");
                    MyLogger.writeToLog("Admin logged out: " + user[0]);
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void register(Scanner scan) {
        System.out.println("\n\t\t Register New User");
        MyLogger.writeToLog("Admin started new user registration.");
        System.out.println("Choose role:");
        System.out.println("1. Dispatcher");
        System.out.println("2. Delivery person");
        int roleChoice;
        try {
            roleChoice = scan.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            MyLogger.writeToLog("Invalid role choice during registration.");
            scan.nextLine();
            return;
        }

        String role;
        if (roleChoice == 1) {
            role = "dispatcher";
        } else if (roleChoice == 2) {
            role = "driver";
        } else {
            System.out.println("Invalid choice.");
            MyLogger.writeToLog("Invalid role number entered during registration.");
            return;
        }

        System.out.println("Username: ");
        String username = scan.next();

        if (UserStore.usernameExists(username)) {
            System.out.println("Username taken.");
            MyLogger.writeToLog("Registration failed: username already taken: " + username);
            return;
        }

        System.out.println("Name: ");
        scan.nextLine();
        String name = scan.nextLine();

        System.out.println("ID number:");
        String idNumber = scan.next();

        System.out.println("Contact number:");
        String contact = scan.next();

        System.out.println("Password:");
        System.out.println("Must have at least ");
        System.out.println(
                PasswordPolicy.getMinDigits() + " Digit\n" + PasswordPolicy.getMinLowercase() + " Lowercase character\n"
                        + PasswordPolicy.getMinUppercase() + " Uppercase character\n" + PasswordPolicy.getMinSpecial()
                        + " Special character\n and at least " + PasswordPolicy.getMinLength() + " characters total");
        String password = scan.next();

        if (!PasswordPolicy.validate(password)) {
            System.out.println("Registration failed: password does not meet requirements.");
            MyLogger.writeToLog("Registration failed: password policy not met for username: " + username);
            return;
        }

        String hash = AuthService.getSHA256Hash(password);
        UserStore.writeUser(
                username + "," + hash + "," + role + "," + name + "," + idNumber + "," + contact + ",false,0");
        System.out.println(role + " registered successfully.");
        MyLogger.writeToLog("Admin registered new " + role + ": " + username);
    }

    private static void removeUser(Scanner scan) {
        System.out.println("\n\t\t\tRemove User");
        MyLogger.writeToLog("Admin started user removal process.");
        viewAllUsers();
        System.out.println("Enter username to remove:");
        String username = scan.next();
        MyLogger.writeToLog("Admin attempting to remove user: " + username);

        String[] target = UserStore.findUser(username);
        if (target == null) {
            System.out.println("User not found.");
            MyLogger.writeToLog("Remove failed: user not found: " + username);
            return;
        }
        if ("admin".equalsIgnoreCase(target[2])) {
            System.out.println("Cannot remove admin account.");
            MyLogger.writeToLog("Remove blocked: attempted to remove admin account: " + username);
            return;
        }
        if (!"dispatcher".equalsIgnoreCase(target[2]) && !"driver".equalsIgnoreCase(target[2])) {
            System.out.println("Can only remove dispatchers or delivery personnel.");
            MyLogger.writeToLog("Remove blocked: invalid role for removal: " + username + " role: " + target[2]);
            return;
        }

        UserStore.removeUser(username);
        System.out.println("User removed: " + username);
        MyLogger.writeToLog("Admin successfully removed user: " + username);
    }

    private static void lockOrUnlock(Scanner scan) {
        System.out.println("\nEnter username:");
        String username = scan.next();
        MyLogger.writeToLog("Admin attempting lock/unlock on account: " + username);
        String[] target = UserStore.findUser(username);
        if (target == null) {
            System.out.println("User not found.");
            MyLogger.writeToLog("Lock/unlock failed: user not found: " + username);
            return;
        }
        if ("admin".equalsIgnoreCase(target[2])) {
            System.out.println("Cannot lock or unlock admin account.");
            MyLogger.writeToLog("Lock/unlock blocked: attempted on admin account: " + username);
            return;
        }

        System.out.println("Account is currently: " + ("true".equalsIgnoreCase(target[6]) ? "Locked" : "Unlocked"));
        System.out.println("1. Lock");
        System.out.println("2. Unlock");
        int lockChoice;
        try {
            lockChoice = scan.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            MyLogger.writeToLog("Invalid input during lock/unlock for: " + username);
            scan.nextLine();
            return;
        }

        if (lockChoice == 1) {
            UserStore.setLocked(username, true);
            System.out.println("Account locked: " + username);
            MyLogger.writeToLog("Admin locked account: " + username);
        } else if (lockChoice == 2) {
            UserStore.setLocked(username, false);
            System.out.println("Account unlocked: " + username);
            MyLogger.writeToLog("Admin unlocked account: " + username);
        } else {
            System.out.println("Invalid choice.");
            MyLogger.writeToLog("Invalid lock/unlock choice for: " + username);
        }
    }

    private static void setPasswordPolicy(Scanner scan) {
        System.out.println("\n\t\t\tPassword Strength Requirements");
        MyLogger.writeToLog("Admin started password policy update.");
        System.out.println("Current Limits:");
        System.out.println("Min length: " + PasswordPolicy.getMinLength());
        System.out.println("Min uppercase: " + PasswordPolicy.getMinUppercase());
        System.out.println("Min lowercase: " + PasswordPolicy.getMinLowercase());
        System.out.println("Min digits: " + PasswordPolicy.getMinDigits());
        System.out.println("Min special chars: " + PasswordPolicy.getMinSpecial());

        try {
            System.out.println("\nEnter minimum length:");
            PasswordPolicy.setMinLength(scan.nextInt());
            System.out.println("Enter minimum uppercase letters:");
            PasswordPolicy.setMinUppercase(scan.nextInt());
            System.out.println("Enter minimum lowercase letters:");
            PasswordPolicy.setMinLowercase(scan.nextInt());
            System.out.println("Enter minimum digits:");
            PasswordPolicy.setMinDigits(scan.nextInt());
            System.out.println("Enter minimum special characters:");
            PasswordPolicy.setMinSpecial(scan.nextInt());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Policy not updated.");
            MyLogger.writeToLog("Password policy update failed: invalid input.");
            scan.nextLine();
            return;
        }

        PasswordPolicy.savePolicy();
        System.out.println("Password policy has been updated.");
        MyLogger.writeToLog("Admin updated password policy: length=" + PasswordPolicy.getMinLength()
                + " upper=" + PasswordPolicy.getMinUppercase()
                + " lower=" + PasswordPolicy.getMinLowercase()
                + " digits=" + PasswordPolicy.getMinDigits()
                + " special=" + PasswordPolicy.getMinSpecial());
    }

    private static void setMax(Scanner scan) {
        System.out.println("\nCurrent max login attempts: " + PasswordPolicy.getMaxLoginAttempts());
        System.out.println("Enter new max login attempts:");
        try {
            PasswordPolicy.setMaxLoginAttempts(scan.nextInt());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            MyLogger.writeToLog("Max login attempts update failed: invalid input.");
            scan.nextLine();
            return;
        }
        PasswordPolicy.savePolicy();
        System.out.println("Max login attempts updated to " + PasswordPolicy.getMaxLoginAttempts());
        MyLogger.writeToLog("Admin updated max login attempts to: " + PasswordPolicy.getMaxLoginAttempts());
    }

    private static void viewAllUsers() {
        System.out.println("\n\t\tAll Users");
        MyLogger.writeToLog("Admin is viewing all users.");
        List<String> lines = UserStore.readAllLines();
        if (lines.isEmpty()) {
            System.out.println("There are no users available.");
            return;
        }
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 8) {
                System.out.println("Username: " + parts[0] + " | Role: " + parts[2]
                        + " | Name: " + parts[3] + " | Locked: " + parts[6]);
            }
        }
    }
}
