package shipTrack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserStore {

    private static final String USERS_FILE = "users.txt";

    private UserStore() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initAdminIfNeeded() {
        if (findUser("admin") == null) {
            String hash = AuthService.getSHA256Hash("Admin@123");
            writeUser("admin," + hash + ",admin,System Admin,0000,0000,false,0");
            MyLogger.writeToLog("Default admin account created.");
        }
    }

    public static String[] findUser(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 8 && parts[0].equalsIgnoreCase(username)) {
                    return parts;
                }
            }
        } catch (Exception e) {
            MyLogger.writeToLog("Error in while reading users file while searching for: " + username);
        }
        return null;
    }

    public static boolean usernameExists(String username) {
        return findUser(username) != null;
    }

    public static void writeUser(String record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(record + "\n");
            MyLogger.writeToLog("New user record written to file.");
        } catch (Exception e) {
            System.out.println("Error writing user.");
            MyLogger.writeToLog("Error writing new user record to file.");
        }
    }

    public static void updateUser(String username, String[] updatedParts) {
        ArrayList<String> lines = readAllLines();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equalsIgnoreCase(username)) {
                    writer.write(String.join(",", updatedParts) + "\n");
                } else {
                    writer.write(line + "\n");
                }
            }
            MyLogger.writeToLog("User record updated: " + username);
        } catch (Exception e) {
            System.out.println("Error updating user.");
            MyLogger.writeToLog("Error updating user record: " + username);
        }
    }

    public static void removeUser(String username) {
        ArrayList<String> lines = readAllLines();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && !parts[0].equalsIgnoreCase(username)) {
                    writer.write(line + "\n");
                }
            }
            MyLogger.writeToLog("User removed from file: " + username);
        } catch (Exception e) {
            System.out.println("Error removing user.");
            MyLogger.writeToLog("Error removing user from file: " + username);
        }
    }

    public static ArrayList<String[]> getAllByRole(String role) {
        ArrayList<String[]> result = new ArrayList<>();
        ArrayList<String> lines = readAllLines();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3 && parts[2].equalsIgnoreCase(role)) {
                result.add(parts);
            }
        }
        MyLogger.writeToLog("Retrieved all users with role: " + role);
        return result;
    }

    public static ArrayList<String> readAllLines() {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (Exception e) {
            MyLogger.writeToLog("Error reading users file.");
        }
        return lines;
    }

    public static boolean isLocked(String[] user) {
        if (user == null || user.length < 7) {
            return false;
        }
        return user[6].equalsIgnoreCase("true");
    }

    public static void incrementFailedAttempts(String[] user) {
        int attempts = Integer.parseInt(user[7]) + 1;
        user[7] = String.valueOf(attempts);
        if (attempts >= PasswordPolicy.getMaxLoginAttempts()) {
            user[6] = "true";
            System.out.println("Account locked due to too many failed attempts.");
            MyLogger.writeToLog("Account locked after max failed attempts: " + user[0]);
        } else {
            MyLogger.writeToLog("Failed attempt " + attempts + " recorded for: " + user[0]);
        }
        updateUser(user[0], user);
    }

    public static void resetFailedAttempts(String[] user) {
        user[7] = "0";
        updateUser(user[0], user);
        MyLogger.writeToLog("Failed attempts reset for: " + user[0]);
    }

    public static void setLocked(String username, boolean locked) {
        String[] user = findUser(username);
        if (user != null) {
            user[6] = String.valueOf(locked);
            if (!locked) {
                user[7] = "0";
            }
            updateUser(username, user);
            MyLogger.writeToLog("Account lock status set to " + locked + " for: " + username);
        } else {
            MyLogger.writeToLog("setLocked failed: user not found: " + username);
        }
    }

public static void updateInfo(Scanner scan, String[] user) {
	System.out.println("\n\t\t\tYour Information");
	MyLogger.writeToLog("Dispatcher " + user[0] + " accessed personal info update.");
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
		MyLogger.writeToLog("Dispatcher " + user[0] + " updated their name.");
		break;
	case 2:
		System.out.println("Enter new contact number:");
		user[5] = scan.next();
		UserStore.updateUser(user[0], user);
		System.out.println("Contact updated.");
		MyLogger.writeToLog("Dispatcher " + user[0] + " updated their contact number.");
		break;
	case 3:
		System.out.println("Enter new password:");
		System.out.println(PasswordPolicy.getMinDigits() + " Digit\n" + PasswordPolicy.getMinLowercase()
				+ " Lowercase character\n" + PasswordPolicy.getMinUppercase() + " Uppercase character\n"
				+ PasswordPolicy.getMinSpecial() + " special character\n and at least have "
				+ PasswordPolicy.getMinLength() + " character");
		String newPassword = scan.next();
		if (!PasswordPolicy.validate(newPassword)) {
			System.out.println("Password does not meet requirements.");
			MyLogger.writeToLog("Dispatcher " + user[0] + " password change failed: policy not met.");
			return;
		}
		user[1] = AuthService.getSHA256Hash(newPassword);
		UserStore.updateUser(user[0], user);
		System.out.println("Password updated.");
		MyLogger.writeToLog("Password changed for Dispatcher: " + user[0]);
		break;
	case 4:
		MyLogger.writeToLog("Dispatcher " + user[0] + " went back from update info.");
		break;
	default:
		System.out.println("Invalid input");
		MyLogger.writeToLog("Invalid choice in update info by dispatcher: " + user[0]);
	}
}
}