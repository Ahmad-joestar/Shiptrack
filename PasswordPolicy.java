import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class PasswordPolicy {

    private static int minLength = 8;
    private static int minUppercase = 1;
    private static int minLowercase = 1;
    private static int minDigits = 1;
    private static int minSpecial = 1;
    private static int maxLoginAttempts = 3;

    private static final String POLICY_FILE = "policy.txt";

    private PasswordPolicy() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        loadPolicy();
    }

    public static void loadPolicy() {
        try (BufferedReader reader = new BufferedReader(new FileReader(POLICY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    int value = Integer.parseInt(parts[1].trim());
                    if ("minLength".equals(key)) {
                        minLength = value;
                    }
                    if ("minUppercase".equals(key)) {
                        minUppercase = value;
                    }
                    if ("minLowercase".equals(key)) {
                        minLowercase = value;
                    }
                    if ("minDigits".equals(key)) {
                        minDigits = value;
                    }
                    if ("minSpecial".equals(key)) {
                        minSpecial = value;
                    }
                    if ("maxLoginAttempts".equals(key)) {
                        maxLoginAttempts = value;
                    }
                }
            }
        } catch (Exception e) {
        	System.err.println("Failed");
        }
    }

    public static void savePolicy() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(POLICY_FILE, false))) {
            writer.write("minLength=" + minLength + "\n");
            writer.write("minUppercase=" + minUppercase + "\n");
            writer.write("minLowercase=" + minLowercase + "\n");
            writer.write("minDigits=" + minDigits + "\n");
            writer.write("minSpecial=" + minSpecial + "\n");
            writer.write("maxLoginAttempts=" + maxLoginAttempts + "\n");
        } catch (Exception e) {
            System.out.println("Error saving policy.");
        }
    }

    public static boolean validate(String password) {
        if (password.length() < minLength) {
            System.out.println("Password must be at least " + minLength + " characters.");
            return false;
        }
        int upper = 0, lower = 0, digit = 0, special = 0;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper++;
            } else if (Character.isLowerCase(c)) {
                lower++;
            } else if (Character.isDigit(c)) {
                digit++;
            } else {
                special++;
            }
        }
        if (upper < minUppercase) {
            System.out.println("Password must have at least " + minUppercase + " uppercase letter(s).");
            return false;
        }
        if (lower < minLowercase) {
            System.out.println("Password must have at least " + minLowercase + " lowercase letter(s).");
            return false;
        }
        if (digit < minDigits) {
            System.out.println("Password must have at least " + minDigits + " digit(s).");
            return false;
        }
        if (special < minSpecial) {
            System.out.println("Password must have at least " + minSpecial + " special character(s).");
            return false;
        }
        return true;
    }

    public static int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public static void setMinLength(int v) {
        minLength = v;
    }

    public static void setMinUppercase(int v) {
        minUppercase = v;
    }

    public static void setMinLowercase(int v) {
        minLowercase = v;
    }

    public static void setMinDigits(int v) {
        minDigits = v;
    }

    public static void setMinSpecial(int v) {
        minSpecial = v;
    }

    public static void setMaxLoginAttempts(int v) {
        maxLoginAttempts = v;
    }

    public static int getMinLength() {
        return minLength;
    }

    public static int getMinUppercase() {
        return minUppercase;
    }

    public static int getMinLowercase() {
        return minLowercase;
    }

    public static int getMinDigits() {
        return minDigits;
    }

    public static int getMinSpecial() {
        return minSpecial;
    }
}