package shipTrack;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {

	public static void register(Scanner scan) {
		System.out.println("\n\t\t\tCustomer Registration");
		MyLogger.writeToLog("Customer registration process started.");
		scan.nextLine();
		String username;
		while (true) {
			System.out.print("Enter a username: ");
			username = scan.nextLine();

			if (UserStore.usernameExists(username)) {
				System.out.println("Username taken. Please try again.");
				MyLogger.writeToLog("Registration failed: username already taken: " + username);
				continue;
			}
			break;
		}

		System.out.print("Enter your name: ");
		String name = scan.nextLine();

		System.out.println("Enter your ID number:");
		String idNumber = scan.next();

		System.out.println("Enter your contact number:");
		String contact = scan.next();

		System.out.println("Enter a password ");
		System.out.println("Must have at least ");
		System.out.println(
				PasswordPolicy.getMinDigits() + " Digit\n" + PasswordPolicy.getMinLowercase() + " Lowercase character\n"
						+ PasswordPolicy.getMinUppercase() + " Uppercase character\n" + PasswordPolicy.getMinSpecial()
						+ " special character\n and at least have " + PasswordPolicy.getMinLength() + " character");
		String password = scan.next();

		if (!PasswordPolicy.validate(password)) {
			System.out.println("Registration failed, password didn't meet the policy, please try again.");
			MyLogger.writeToLog("Registration failed: password policy not met for username: " + username);
			return;
		}

		String hash = AuthService.getSHA256Hash(password);
		UserStore.writeUser(username + "," + hash + ",customer," + name + "," + idNumber + "," + contact + ",false,0");
		System.out.println("Registration successful! You can now log in.");
		MyLogger.writeToLog("New customer registered successfully: " + username);
	}

	public static void scan(Scanner scan, String[] user) {
		MyLogger.writeToLog("Customer menu accessed by: " + user[0]);
		boolean running = true;
		while (running) {
			System.out.println("\n\t\t\tCustomer Menu");
			System.out.println("1. Create shipment request");
			System.out.println("2. Track shipments");
			System.out.println("3. View/Update personal information");
			System.out.println("4. Logout");
			System.out.println("Choose an option(Pick a number):");
			int choice;

			try {
				choice = scan.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please enter a number.");
				MyLogger.writeToLog("Invalid menu input by customer: " + user[0]);
				scan.nextLine();
				continue;
			}

			switch (choice) {
			case 1:
				createShipment(scan, user[0]);
				break;
			case 2:
				trackShipments(user[0]);
				break;
			case 3:
				UserStore.updateInfo(scan, user);
				break;
			case 4:
				running = false;
				System.out.println("Logged out.");
				MyLogger.writeToLog("Customer logged out: " + user[0]);
				break;
			default:
				System.out.println("Invalid option. Try again.");
			}
		}
	}

	private static void createShipment(Scanner scan, String username) {
		System.out.println("\n\t\t\tRequest a shipment: ");
		MyLogger.writeToLog("Customer " + username + " started shipment creation.");
		System.out.println("Enter Your source :");
		scan.nextLine();
		String origin = scan.nextLine();

		System.out.println("Enter the destination:");
		String destination = scan.nextLine();

		ShipmentStore.createShipment(username, origin, destination);
		System.out.println("Shipment request created successfully.");
	}

	private static void trackShipments(String username) {
		System.out.println("\n\t\t\tYour Shipments");
		MyLogger.writeToLog("Customer " + username + " is tracking their shipments.");
		List<String[]> shipments = ShipmentStore.getShipmentsByCustomer(username);
		if (shipments.isEmpty()) {
			System.out.println("No shipments found.");
			return;
		}
		for (String[] s : shipments) {
			System.out.println("ID: " + s[0] + " | From: " + s[3] + " | To: " + s[4] + " | Status: " + s[5]);
		}
	}
}