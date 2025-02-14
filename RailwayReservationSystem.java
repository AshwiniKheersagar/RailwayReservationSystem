package Project;

import java.sql.*;
import java.util.Scanner;

public class RailwayReservationSystem {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "2004";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Ensure the 'tickets' table exists and initialize if not
            initializeDatabase(conn);

            do {
                displayMenu();
                choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        bookTicket(scanner, conn);
                        break;
                    case 2:
                        cancelTicket(scanner, conn);
                        break;
                    case 3:
                        displayBookedTickets(conn);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            } while (choice != 4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void displayMenu() {
        System.out.println("\nRailway Reservation System");
        System.out.println("1. Book a Ticket");
        System.out.println("2. Cancel a Ticket");
        System.out.println("3. Display Booked Tickets");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        // Check if the 'tickets' table exists
        if (!tableExists(conn, "tickets")) {
            // Create 'tickets' table
            String createTableSQL = "CREATE TABLE tickets ("
                    + "seat_number NUMBER(2) PRIMARY KEY,"
                    + "is_booked NUMBER(1,0) DEFAULT 0"
                    + ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table 'tickets' created successfully.");

                // Insert initial seat numbers 1 to 10
                String insertSQL = "INSERT INTO tickets (seat_number) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                    for (int i = 1; i <= 10; i++) {
                        ps.setInt(1, i);
                        ps.executeUpdate();
                    }
                    System.out.println("Initial seat numbers inserted into 'tickets' table.");
                } catch (SQLException e) {
                    System.out.println("Error inserting initial seat numbers: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.out.println("Error creating table 'tickets': " + e.getMessage());
            }
        } else {
            System.out.println("Table 'tickets' already exists.");
        }
    }


    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet tables = meta.getTables(null, null, tableName.toUpperCase(), new String[] {"TABLE"});
        return tables.next();
    }

    public static void bookTicket(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter seat number to book (1-10): ");
        int seatNumber = scanner.nextInt();
        if (seatNumber < 1 || seatNumber > 10) {
            System.out.println("Invalid seat number. Please choose a seat number between 1 and 10.");
            return;
        }

        // Check if the seat exists in the 'tickets' table
        String queryCheck = "SELECT is_booked FROM tickets WHERE seat_number = ?";
        String queryBook = "UPDATE tickets SET is_booked = 1 WHERE seat_number = ?";

        try (PreparedStatement psCheck = conn.prepareStatement(queryCheck);
             PreparedStatement psBook = conn.prepareStatement(queryBook)) {

            psCheck.setInt(1, seatNumber);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                int isBooked = rs.getInt("is_booked");
                if (isBooked == 0) {
                    psBook.setInt(1, seatNumber);
                    psBook.executeUpdate();
                    System.out.println("Seat number " + seatNumber + " has been successfully booked.");
                } else {
                    System.out.println("Seat number " + seatNumber + " is already booked.");
                }
            } else {
                System.out.println("Seat number " + seatNumber + " does not exist in the database.");
            }
        }
    }



    public static void cancelTicket(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter seat number to cancel (1-10): ");
        int seatNumber = scanner.nextInt();
        if (seatNumber < 1 || seatNumber > 10) {
            System.out.println("Invalid seat number. Please choose a seat number between 1 and 10.");
            return;
        }

        String queryCheck = "SELECT is_booked FROM tickets WHERE seat_number = ?";
        String queryCancel = "UPDATE tickets SET is_booked = 0 WHERE seat_number = ?";

        try (PreparedStatement psCheck = conn.prepareStatement(queryCheck);
             PreparedStatement psCancel = conn.prepareStatement(queryCancel)) {

            psCheck.setInt(1, seatNumber);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                if (rs.getInt("is_booked") == 1) {
                    psCancel.setInt(1, seatNumber);
                    psCancel.executeUpdate();
                    System.out.println("Seat number " + seatNumber + " has been successfully canceled.");
                } else {
                    System.out.println("Seat number " + seatNumber + " is not booked yet.");
                }
            }
        }
    }

    public static void displayBookedTickets(Connection conn) throws SQLException {
        String query = "SELECT seat_number FROM tickets WHERE is_booked = 1";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Booked Tickets:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("Seat " + rs.getInt("seat_number"));
                found = true;
            }
            if (!found) {
                System.out.println("No seats are currently booked.");
            }
        }
    }
}
