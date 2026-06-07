package shipTrack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.ArrayList;

public class loginTest {

	@BeforeEach
	public void setUp() {
		new File("users.txt").delete();
		new File("logfile.log").delete();
		UserStore.initAdminIfNeeded();
	}

	@AfterEach
	public void tearDown() {
		new File("users.txt").delete();
		new File("logfile.log").delete();
	}

	@Test
	public void testValidLogin() {
		String[] user = UserStore.findUser("admin");
		assertNotNull(user, "Admin user should exist");
		String inputHash = AuthService.getSHA256Hash("Admin@123");
		assertEquals(user[1], inputHash, "Password hash should match");
	}

	@Test
	public void testWrongCredintials() {
		String[] user = UserStore.findUser("admin");
		assertNotNull(user);
		String inputHash = AuthService.getSHA256Hash("wrongpass");
		assertNotEquals(user[1], inputHash, "Wrong password should not match hash");
	}

	@Test
	public void testAccountExceedsMaxAttempts() {
		String hash = AuthService.getSHA256Hash("Pass@1234");
		UserStore.writeUser("testcust," + hash + ",customer,Test User,111,0799999999,false,0");

		String[] user = UserStore.findUser("testcust");
		assertNotNull(user);

		UserStore.incrementFailedAttempts(user);
		user = UserStore.findUser("testcust");
		UserStore.incrementFailedAttempts(user);
		user = UserStore.findUser("testcust");
		UserStore.incrementFailedAttempts(user);

		user = UserStore.findUser("testcust");
		assertTrue(UserStore.isLocked(user), "Account should be locked after max failed attempts");
	}

	@Test
	public void testLockedAccountCannotLogin() {
		String hash = AuthService.getSHA256Hash("Pass@1234");
		UserStore.writeUser("lockeduser," + hash + ",customer,Locked User,222,0791111111,true,3");

		String[] user = UserStore.findUser("lockeduser");
		assertNotNull(user);
		assertTrue(UserStore.isLocked(user), "Account should already be locked");
	}
}