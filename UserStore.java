import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

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
            MyLogger.writeToLog("Error reading users file while searching for: " + username);
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
}
