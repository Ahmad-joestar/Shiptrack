package shipTrack;
 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.util.Scanner;
 
public class CustomerRegistrationTest {
 
    @BeforeEach
    public void setUp() {
        new File("users.txt").delete();
        new File("logfile.log").delete();
        UserStore.initAdminIfNeeded();
        PasswordPolicy.loadPolicy();
    }
 
    @AfterEach
    public void tearDown() {
        new File("users.txt").delete();
        new File("logfile.log").delete();
    }
 
    private Scanner getSimulatedScanner(String inputData) {
        return new Scanner(new ByteArrayInputStream(inputData.getBytes()));
    }
 
    @Test
    public void testValidRegistration() {
        String input = "\nnewcust\nTest Customer\n111222\n0790000000\nStrongPass1!\n";
        Scanner scan = getSimulatedScanner(input);
        CustomerMenu.register(scan);
        String[] user = UserStore.findUser("newcust");
        assertNotNull(user, "Customer user should exist");
        assertEquals("customer", user[2], "Role should be customer");
    }
 
    @Test
    public void testUsernameTaken() {
        String input = "\nadmin\nValid Customer\n333444\n0791111111\nStrongPass1!\n";
        Scanner scan = getSimulatedScanner(input);
        CustomerMenu.register(scan);
        String[] user = UserStore.findUser("validcust");
        assertNotNull(user, "System should bypass taken username and register the valid one");
    }
 
    @Test
    public void testWrongPasswordPolicy() {
        String input = "\nbadpasscust\nBad Customer\n555666\n0792222222\nweak1234\n";
        Scanner scan = getSimulatedScanner(input);
        CustomerMenu.register(scan);
        String[] user = UserStore.findUser("badpasscust");
        assertNull(user, "Registration should fail and return null for weak passwords");
    }
}