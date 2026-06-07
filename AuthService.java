
package shipTrack;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AuthService {

    private AuthService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String[] login(Scanner scan) {
        System.out.println("Enter username:");
        String username = scan.next();
        System.out.println("Enter password:");
        String password = scan.next();

        MyLogger.writeToLog("Login attempt for username: " + username);

        String[] user = UserStore.findUser(username);

        if (user == null) {
            System.out.println("Login failed: Invalid credentials.");
            MyLogger.writeToLog("Login failed: username not found: " + username);
            return null;
        }

        if (UserStore.isLocked(user)) {
            System.out.println("Account is locked. To unlock it, talk with admin");
            MyLogger.writeToLog("Login blocked: account is locked: " + username);
            return null;
        }

        String inputHash = getSHA256Hash(password);
        if (!user[1].equals(inputHash)) {
            System.out.println("Login failed: Invalid credentials.");
            MyLogger.writeToLog("Login failed: wrong password for username: " + username);
            UserStore.incrementFailedAttempts(user);
            return null;
        }

        UserStore.resetFailedAttempts(user);
        System.out.println("Login successful. Welcome, " + user[3]);
        MyLogger.writeToLog("Successful login: " + username );
        return user;
    }

    public static String getSHA256Hash(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                plaintext.getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not found.");
            MyLogger.writeToLog("Error: SHA-256 algorithm not found during hashing.");
            return null;
        }
    }
}
