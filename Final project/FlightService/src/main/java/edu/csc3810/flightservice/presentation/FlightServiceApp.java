package edu.csc3810.flightservice.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.business.BookingService;
import edu.csc3810.flightservice.business.ClientService;
import edu.csc3810.flightservice.business.FlightService;
import edu.csc3810.flightservice.business.ServiceException;
import edu.csc3810.flightservice.dal.AirportDAO;
import edu.csc3810.flightservice.dal.BookingDAO;
import edu.csc3810.flightservice.dal.ClientDAO;
import edu.csc3810.flightservice.dal.FlightDAO;
import edu.csc3810.flightservice.model.Booking;
import edu.csc3810.flightservice.model.Client;
import edu.csc3810.flightservice.model.Flight;

/**
 * Main presentation layer application for the Flight Service.
 */
public class FlightServiceApp {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightServiceApp.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final Scanner scanner;
    private final FlightService flightService;
    private final BookingService bookingService;
    private final ClientService clientService;
    
    /**
     * Constructor initializing services.
     */
    public FlightServiceApp() {
        scanner = new Scanner(System.in);
        
        // Initialize DAOs
        AirportDAO airportDAO = new AirportDAO();
        FlightDAO flightDAO = new FlightDAO();
        BookingDAO bookingDAO = new BookingDAO();
        ClientDAO clientDAO = new ClientDAO();
        
        // Initialize services
        flightService = new FlightService(flightDAO, airportDAO);
        bookingService = new BookingService(bookingDAO, clientDAO, flightDAO);
        clientService = new ClientService(clientDAO);
    }
    
    /**
     * Main application method.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        FlightServiceApp app = new FlightServiceApp();
        app.run();
    }
    
    /**
     * Main application loop.
     */
    public void run() {
        System.out.println("Welcome to the Flight Service System");
        
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    searchFlights();
                    break;
                case 2:
                    bookFlight();
                    break;
                case 3:
                    viewBookings();
                    break;
                case 4:
                    cancelBooking();
                    break;
                case 5:
                    redeemPoints();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using Flight Service. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    /**
     * Displays the main menu.
     */
    private void displayMainMenu() {
        System.out.println("\n=== Flight Service Main Menu ===");
        System.out.println("1. Search Flights");
        System.out.println("2. Book a Flight");
        System.out.println("3. View My Bookings");
        System.out.println("4. Cancel a Booking");
        System.out.println("5. Redeem Reward Points");
        System.out.println("0. Exit");
    }
    
    /**
     * Workflow 1: Search for flights.
     */
    private void searchFlights() {
        System.out.println("\n=== Search Flights ===");
        
        String originCode = getStringInput("Enter origin airport code (e.g., JFK): ");
        String destinationCode = getStringInput("Enter destination airport code (e.g., LAX): ");
        
        LocalDate departureDate = null;
        while (departureDate == null) {
            String dateStr = getStringInput("Enter departure date (YYYY-MM-DD): ");
            try {
                departureDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        
        try {
            List<Flight> flights = flightService.searchFlights(originCode, destinationCode, departureDate);
            
            if (flights.isEmpty()) {
                System.out.println("No flights found matching your criteria.");
            } else {
                System.out.println("\nAvailable Flights:");
                System.out.println("-----------------------------------------------------------------");
                System.out.printf("%-8s %-15s %-15s %-20s %-10s %-8s%n", 
                        "ID", "Flight", "Route", "Departure", "Price", "Seats");
                System.out.println("-----------------------------------------------------------------");
                
                for (Flight flight : flights) {
                    System.out.printf("%-8d %-15s %-15s %-20s $%-9.2f %-8d%n",
                            flight.getFlightId(),
                            flight.getFlightNumber(),
                            flight.getOriginAirportCode() + " → " + flight.getDestinationAirportCode(),
                            flight.getDepartureDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            flight.getBasePrice(),
                            flight.getAvailableSeats());
                }
                System.out.println("-----------------------------------------------------------------");
            }
        } catch (ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error searching flights", e);
        }
    }
    
    /**
     * Workflow 2: Book a flight.
     */
    private void bookFlight() {
        System.out.println("\n=== Book a Flight ===");
        
        // First, list available clients
        try {
            List<Client> clients = clientService.getAllClients();
            System.out.println("\nAvailable Clients:");
            for (Client client : clients) {
                System.out.printf("ID: %d - %s (%s)%n", 
                        client.getClientId(), client.getClientName(), client.getEmail());
            }
        } catch (ServiceException e) {
            System.out.println("Error retrieving clients: " + e.getMessage());
            return;
        }
        
        int clientId = getIntInput("Enter client ID: ");
        
        // Prompt for flight search criteria
        String originCode = getStringInput("Enter origin airport code (e.g., JFK): ");
        String destinationCode = getStringInput("Enter destination airport code (e.g., LAX): ");
        
        LocalDate departureDate = null;
        while (departureDate == null) {
            String dateStr = getStringInput("Enter departure date (YYYY-MM-DD): ");
            try {
                departureDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        
        try {
            // Search for flights
            List<Flight> flights = flightService.searchFlights(originCode, destinationCode, departureDate);
            
            if (flights.isEmpty()) {
                System.out.println("No flights found matching your criteria.");
                return;
            }
            
            // Display available flights
            System.out.println("\nAvailable Flights:");
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-8s %-15s %-15s %-20s %-10s %-8s%n", 
                    "ID", "Flight", "Route", "Departure", "Price", "Seats");
            System.out.println("-----------------------------------------------------------------");
            
            for (Flight flight : flights) {
                System.out.printf("%-8d %-15s %-15s %-20s $%-9.2f %-8d%n",
                        flight.getFlightId(),
                        flight.getFlightNumber(),
                        flight.getOriginAirportCode() + " → " + flight.getDestinationAirportCode(),
                        flight.getDepartureDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        flight.getBasePrice(),
                        flight.getAvailableSeats());
            }
            System.out.println("-----------------------------------------------------------------");
            
            // Prompt for flight selection
            int flightId = getIntInput("Enter flight ID to book: ");
            String seatNumber = getStringInput("Enter seat number (e.g., 12A): ");
            
            // Book the flight
            int bookingId = bookingService.bookFlight(clientId, flightId, seatNumber);
            System.out.println("Flight booked successfully! Booking ID: " + bookingId);
            
            // Prompt for passenger details
            System.out.println("\nEnter passenger details:");
            String firstName = getStringInput("First name: ");
            String lastName = getStringInput("Last name: ");
            String passportNumber = getStringInput("Passport number: ");
            String nationality = getStringInput("Nationality: ");
            String specialRequirements = getStringInput("Special requirements (comma-separated, leave empty if none): ");
            
            // Update passenger details
            bookingService.updatePassengerDetails(bookingId, firstName, lastName, passportNumber, nationality, specialRequirements);
            System.out.println("Passenger details updated successfully!");
            
        } catch (ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error booking flight", e);
        }
    }
    
    /**
     * Workflow 3: View bookings for a client.
     */
    private void viewBookings() {
        System.out.println("\n=== View My Bookings ===");
        
        // First, list available clients
        try {
            List<Client> clients = clientService.getAllClients();
            System.out.println("\nAvailable Clients:");
            for (Client client : clients) {
                System.out.printf("ID: %d - %s (%s)%n", 
                        client.getClientId(), client.getClientName(), client.getEmail());
            }
        } catch (ServiceException e) {
            System.out.println("Error retrieving clients: " + e.getMessage());
            return;
        }
        
        int clientId = getIntInput("Enter client ID: ");
        
        try {
            List<Booking> bookings = bookingService.getClientBookings(clientId);
            
            if (bookings.isEmpty()) {
                System.out.println("No bookings found for this client.");
            } else {
                System.out.println("\nClient Bookings:");
                System.out.println("----------------------------------------------------------------------");
                System.out.printf("%-8s %-10s %-15s %-20s %-8s %-10s%n", 
                        "ID", "Flight", "Route", "Departure", "Seat", "Status");
                System.out.println("----------------------------------------------------------------------");
                
                for (Booking booking : bookings) {
                    System.out.printf("%-8d %-10s %-15s %-20s %-8s %-10s%n",
                            booking.getBookingId(),
                            booking.getFlightNumber(),
                            booking.getOriginAirportCode() + " → " + booking.getDestinationAirportCode(),
                            booking.getDepartureDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            booking.getSeatNumber(),
                            booking.getStatus());
                }
                System.out.println("----------------------------------------------------------------------");
                
                // Display reward points
                int rewardPoints = clientService.getClientRewardPoints(clientId);
                System.out.println("\nAvailable Reward Points: " + rewardPoints);
            }
        } catch (ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error retrieving bookings", e);
        }
    }
    
    /**
     * Workflow 4: Cancel a booking.
     */
    private void cancelBooking() {
        System.out.println("\n=== Cancel a Booking ===");
        
        // First, list available clients
        try {
            List<Client> clients = clientService.getAllClients();
            System.out.println("\nAvailable Clients:");
            for (Client client : clients) {
                System.out.printf("ID: %d - %s (%s)%n", 
                        client.getClientId(), client.getClientName(), client.getEmail());
            }
        } catch (ServiceException e) {
            System.out.println("Error retrieving clients: " + e.getMessage());
            return;
        }
        
        int clientId = getIntInput("Enter client ID: ");
        
        try {
            // Display client's bookings
            List<Booking> bookings = bookingService.getClientBookings(clientId);
            
            if (bookings.isEmpty()) {
                System.out.println("No bookings found for this client.");
                return;
            }
            
            System.out.println("\nClient Bookings:");
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("%-8s %-10s %-15s %-20s %-8s %-10s%n", 
                    "ID", "Flight", "Route", "Departure", "Seat", "Status");
            System.out.println("----------------------------------------------------------------------");
            
            for (Booking booking : bookings) {
                System.out.printf("%-8d %-10s %-15s %-20s %-8s %-10s%n",
                        booking.getBookingId(),
                        booking.getFlightNumber(),
                        booking.getOriginAirportCode() + " → " + booking.getDestinationAirportCode(),
                        booking.getDepartureDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        booking.getSeatNumber(),
                        booking.getStatus());
            }
            System.out.println("----------------------------------------------------------------------");
            
            // Prompt for booking ID to cancel
            int bookingId = getIntInput("Enter booking ID to cancel: ");
            
            // Confirm cancellation
            String confirm = getStringInput("Are you sure you want to cancel this booking? (y/n): ");
            if (confirm.equalsIgnoreCase("y")) {
                bookingService.cancelBooking(bookingId);
                System.out.println("Booking cancelled successfully!");
            } else {
                System.out.println("Cancellation aborted.");
            }
            
        } catch (ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error cancelling booking", e);
        }
    }
    
    /**
     * Workflow 5: Redeem reward points for a discount.
     */
    private void redeemPoints() {
        System.out.println("\n=== Redeem Reward Points ===");
        
        // First, list available clients
        try {
            List<Client> clients = clientService.getAllClients();
            System.out.println("\nAvailable Clients:");
            for (Client client : clients) {
                System.out.printf("ID: %d - %s (%s)%n", 
                        client.getClientId(), client.getClientName(), client.getEmail());
            }
        } catch (ServiceException e) {
            System.out.println("Error retrieving clients: " + e.getMessage());
            return;
        }
        
        int clientId = getIntInput("Enter client ID: ");
        
        try {
            // Display client's reward points
            int rewardPoints = clientService.getClientRewardPoints(clientId);
            System.out.println("\nAvailable Reward Points: " + rewardPoints);
            
            if (rewardPoints <= 0) {
                System.out.println("You don't have any reward points to redeem.");
                return;
            }
            
            // Display client's bookings
            List<Booking> bookings = bookingService.getClientBookings(clientId);
            
            if (bookings.isEmpty()) {
                System.out.println("No bookings found for this client.");
                return;
            }
            
            System.out.println("\nClient Bookings:");
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("%-8s %-10s %-15s %-20s %-8s %-10s%n", 
                    "ID", "Flight", "Route", "Departure", "Seat", "Status");
            System.out.println("----------------------------------------------------------------------");
            
            for (Booking booking : bookings) {
                if ("Confirmed".equals(booking.getStatus())) {
                    System.out.printf("%-8d %-10s %-15s %-20s %-8s %-10s%n",
                            booking.getBookingId(),
                            booking.getFlightNumber(),
                            booking.getOriginAirportCode() + " → " + booking.getDestinationAirportCode(),
                            booking.getDepartureDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            booking.getSeatNumber(),
                            booking.getStatus());
                }
            }
            System.out.println("----------------------------------------------------------------------");
            
            // Prompt for booking and points
            int bookingId = getIntInput("Enter booking ID to apply discount: ");
            int pointsToRedeem = getIntInput("Enter number of points to redeem (1 point = $0.01, max 50% of flight cost): ");
            
            // Confirm redemption
            String confirm = getStringInput("Are you sure you want to redeem " + pointsToRedeem + " points? (y/n): ");
            if (confirm.equalsIgnoreCase("y")) {
                double discountAmount = bookingService.redeemPointsForDiscount(clientId, pointsToRedeem, bookingId);
                System.out.printf("Points redeemed successfully! Discount amount: $%.2f%n", discountAmount);
                
                // Display remaining points
                int remainingPoints = clientService.getClientRewardPoints(clientId);
                System.out.println("Remaining Reward Points: " + remainingPoints);
            } else {
                System.out.println("Redemption aborted.");
            }
            
        } catch (ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error redeeming points", e);
        }
    }
    
    /**
     * Gets a string input from the user.
     * 
     * @param prompt The prompt to display
     * @return The input string
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    /**
     * Gets an integer input from the user.
     * 
     * @param prompt The prompt to display
     * @return The input integer
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
} 