package shipTrack;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class DispatcherMenu {

	private DispatcherMenu() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static void menu(Scanner scan, String[] user) {
		MyLogger.writeToLog("Dispatcher menu accessed by: " + user[0]);
		boolean running = true;
		while (running) {
			System.out.println("\n\t\t\tDispatcher Menu");
			System.out.println("1. View shipments");
			System.out.println("2. Assign delivery to driver");
			System.out.println("3. Update delivery status");
			System.out.println("4. View/Update personal information");
			System.out.println("5. Logout");
			System.out.println("Choose an option(Pick a number):");
			int choice;

			try {
				choice = scan.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please enter a number.");
				MyLogger.writeToLog("Invalid menu input by dispatcher: " + user[0]);
				scan.nextLine();
				continue;
			}

			switch (choice) {
			case 1:
				viewAllShipments(user[0]);
				break;
			case 2:
				assignDelivery(scan, user[0]);
				break;
			case 3:
				updateDeliveryStatus(scan, user[0]);
				break;
			case 4:
				UserStore.updateInfo(scan, user);
				break;
			case 5:
				running = false;
				System.out.println("Logged out.");
				MyLogger.writeToLog("Dispatcher logged out: " + user[0]);
				break;
			default:
				System.out.println("Invalid option. Try again.");
				MyLogger.writeToLog("Invalid option selected in dispatcher menu by: " + user[0]);
			}
		}
	}

	private static void viewAllShipments(String username) {
		System.out.println("\n\t\t\tAll Shipments");
		MyLogger.writeToLog("Dispatcher " + username + " is viewing all shipments.");
		List<String[]> shipments = ShipmentStore.getAllShipments();
		if (shipments.isEmpty()) {
			System.out.println("No shipments found.");
			return;
		}
		for (String[] s : shipments) {
			System.out.println("ID: " + s[0] + " | Customer: " + s[1] + " | Driver: " + s[2] + " | From: " + s[3]
					+ " | To: " + s[4] + " | Status: " + s[5]);
		}
	}

	private static void assignDelivery(Scanner scan, String username) {
		viewAllShipments(username);
		System.out.println("\nEnter shipment ID to assign:");
		String shipmentID = scan.next();

		System.out.println("Available drivers:");
		List<String[]> drivers = UserStore.getAllByRole("driver");
		if (drivers.isEmpty()) {
			System.out.println("No drivers available.");
			MyLogger.writeToLog("Assign failed: no drivers available.");
			return;
		}
		for (String[] d : drivers) {
			System.out.println("Username: " + d[0] + " | Name: " + d[3]);
		}

		System.out.println("Enter driver username:");
		String driverUsername = scan.next();

		if (UserStore.findUser(driverUsername) == null) {
			System.out.println("Driver not found.");
			MyLogger.writeToLog("Assign failed: driver not found: " + driverUsername);
			return;
		}

		if (ShipmentStore.findShipment(shipmentID) == null) {
			System.out.println("Shipment not found.");
			MyLogger.writeToLog("Assign failed: shipment not found: " + shipmentID);
			return;
		}

		ShipmentStore.assignDriver(shipmentID, driverUsername);
		System.out.println("Shipment assigned to driver: " + driverUsername);
	}

	private static void updateDeliveryStatus(Scanner scan, String username) {
		viewAllShipments(username);
		System.out.println("\nEnter shipment ID to update:");
		String shipmentID = scan.next();
		MyLogger.writeToLog("Dispatcher " + username + " attempting to update status for shipment: " + shipmentID);

		if (ShipmentStore.findShipment(shipmentID) == null) {
			System.out.println("Shipment not found.");
			MyLogger.writeToLog("Update status failed: shipment not found: " + shipmentID);
			return;
		}

		System.out.println("Choose new status:");
		System.out.println("1. pending");
		System.out.println("2. in transit");
		System.out.println("3. delivered");
		int choice;
		try {
			choice = scan.nextInt();
		} catch (InputMismatchException e) {
			System.out.println("Invalid input.");
			MyLogger.writeToLog("Invalid status input by dispatcher: " + username);
			scan.nextLine();
			return;
		}
		String newStatus = "";
		switch (choice) {
		case 1:
			newStatus = "pending";
			break;
		case 2:
			newStatus = "in transit";
			break;
		case 3:
			newStatus = "delivered";
			break;
		default:
			System.out.println("Invalid choice.");
			MyLogger.writeToLog("Invalid status choice by dispatcher: " + username);
			return;
		}
		ShipmentStore.updateStatus(shipmentID, newStatus);
		System.out.println("Status updated to: " + newStatus);
	}

}
