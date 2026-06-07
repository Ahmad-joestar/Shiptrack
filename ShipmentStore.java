package shipTrack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ShipmentStore {

    private static final String SHIPMENTS_FILE = "shipments.txt";

    private ShipmentStore() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateID() {
        return "SHP" + System.currentTimeMillis();
    }

    public static void createShipment(String customerUsername, String origin, String destination) {
        String id = generateID();
        String record = id + "," + customerUsername + ",unassigned," + origin + "," + destination + ",pending";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SHIPMENTS_FILE, true))) {
            writer.write(record + "\n");
            MyLogger.writeToLog("Shipment created: " + id + " by customer: " + customerUsername + " from: " + origin + " to: " + destination);
        } catch (Exception e) {
            System.out.println("Error creating shipment.");
            MyLogger.writeToLog("Error creating shipment for customer: " + customerUsername);
        }
    }

    public static List<String[]> getShipmentsByCustomer(String customerUsername) {
        List<String[]> result = new ArrayList<>();
        for (String[] parts : readAll()) {
            if (parts.length >= 6 && parts[1].equalsIgnoreCase(customerUsername)) {
                result.add(parts);
            }
        }
        MyLogger.writeToLog("Customer " + customerUsername + " retrieved their shipments. Count: " + result.size());
        return result;
    }

    public static List<String[]> getShipmentsByDriver(String driverUsername) {
        List<String[]> result = new ArrayList<>();
        for (String[] parts : readAll()) {
            if (parts.length >= 6 && parts[2].equalsIgnoreCase(driverUsername)) {
                result.add(parts);
            }
        }
        MyLogger.writeToLog("Driver " + driverUsername + " retrieved assigned shipments. Count: " + result.size());
        return result;
    }

    public static List<String[]> getAllShipments() {
        List<String[]> result = readAll();
        MyLogger.writeToLog("All shipments retrieved. Count: " + result.size());
        return result;
    }

    public static String[] findShipment(String shipmentID) {
        for (String[] parts : readAll()) {
            if (parts.length >= 6 && parts[0].equalsIgnoreCase(shipmentID)) {
                MyLogger.writeToLog("Shipment found: " + shipmentID);
                return parts;
            }
        }
        MyLogger.writeToLog("Shipment not found: " + shipmentID);
        return null;
    }

    public static void updateShipment(String shipmentID, String[] updatedParts) {
        List<String[]> all = readAll();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SHIPMENTS_FILE, false))) {
            for (String[] parts : all) {
                if (parts[0].equalsIgnoreCase(shipmentID)) {
                    writer.write(String.join(",", updatedParts) + "\n");
                } else {
                    writer.write(String.join(",", parts) + "\n");
                }
            }
            MyLogger.writeToLog("Shipment record updated: " + shipmentID);
        } catch (Exception e) {
            System.out.println("Error updating shipment.");
            MyLogger.writeToLog("Error updating shipment record: " + shipmentID);
        }
    }

    public static void assignDriver(String shipmentID, String driverUsername) {
        String[] shipment = findShipment(shipmentID);
        if (shipment != null) {
            shipment[2] = driverUsername;
            shipment[5] = "in transit";
            updateShipment(shipmentID, shipment);
            MyLogger.writeToLog("Shipment " + shipmentID + " assigned to driver: " + driverUsername);
        } else {
            System.out.println("Shipment not found.");
            MyLogger.writeToLog("Assign driver failed: shipment not found: " + shipmentID);
        }
    }

    public static void updateStatus(String shipmentID, String newStatus) {
        String[] shipment = findShipment(shipmentID);
        if (shipment != null) {
            String oldStatus = shipment[5];
            shipment[5] = newStatus;
            updateShipment(shipmentID, shipment);
            MyLogger.writeToLog("Shipment " + shipmentID + " status changed from: " + oldStatus + " to: " + newStatus);
        } else {
            System.out.println("Shipment not found.");
            MyLogger.writeToLog("Update status failed: shipment not found: " + shipmentID);
        }
    }

    private static List<String[]> readAll() {
        List<String[]> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(SHIPMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim().split(","));
                }
            }
        } catch (Exception e) {
            MyLogger.writeToLog("Error reading shipments file.");
        }
        return result;
    }
}
