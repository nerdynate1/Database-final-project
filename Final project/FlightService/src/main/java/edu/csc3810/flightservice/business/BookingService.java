package edu.csc3810.flightservice.business;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.csc3810.flightservice.dal.BookingDAO;
import edu.csc3810.flightservice.dal.ClientDAO;
import edu.csc3810.flightservice.dal.FlightDAO;
import edu.csc3810.flightservice.model.Booking;
import edu.csc3810.flightservice.model.Client;
import edu.csc3810.flightservice.model.Flight;

/**
 * Business logic for booking operations.
 */
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final Pattern SEAT_PATTERN = Pattern.compile("^[0-9]{1,2}[A-F]$");
    
    private final BookingDAO bookingDAO;
    private final ClientDAO clientDAO;
    private final FlightDAO flightDAO;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param bookingDAO The booking data access object
     * @param clientDAO The client data access object
     * @param flightDAO The flight data access object
     */
    public BookingService(BookingDAO bookingDAO, ClientDAO clientDAO, FlightDAO flightDAO) {
        this.bookingDAO = bookingDAO;
        this.clientDAO = clientDAO;
        this.flightDAO = flightDAO;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Books a flight for a client.
     * 
     * @param clientId The client ID
     * @param flightId The flight ID
     * @param seatNumber The seat number
     * @return The booking ID
     * @throws ServiceException If validation fails or booking fails
     */
    public int bookFlight(int clientId, int flightId, String seatNumber) throws ServiceException {
        logger.info("Booking flight ID {} for client ID {} with seat {}", flightId, clientId, seatNumber);
        
        // Validate inputs
        if (clientId <= 0) {
            throw new ServiceException("Invalid client ID");
        }
        if (flightId <= 0) {
            throw new ServiceException("Invalid flight ID");
        }
        if (seatNumber == null || !SEAT_PATTERN.matcher(seatNumber).matches()) {
            throw new ServiceException("Invalid seat number format (should be like 1A, 12F)");
        }
        
        // Verify client exists
        Client client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ServiceException("Client not found with ID: " + clientId);
        }
        
        // Verify flight exists and has available seats
        Flight flight = flightDAO.getFlightById(flightId);
        if (flight == null) {
            throw new ServiceException("Flight not found with ID: " + flightId);
        }
        
        // Book the flight
        int bookingId = bookingDAO.bookFlight(clientId, flightId, seatNumber);
        if (bookingId <= 0) {
            throw new ServiceException("Flight booking failed");
        }
        
        logger.debug("Flight booked successfully with booking ID: {}", bookingId);
        return bookingId;
    }
    
    /**
     * Updates passenger details for a booking.
     * 
     * @param bookingId The booking ID
     * @param firstName The passenger's first name
     * @param lastName The passenger's last name
     * @param passportNumber The passenger's passport number
     * @param nationality The passenger's nationality
     * @param specialRequirements Special requirements (comma-separated)
     * @throws ServiceException If validation fails or update fails
     */
    public void updatePassengerDetails(int bookingId, String firstName, String lastName, 
                                       String passportNumber, String nationality, String specialRequirements) 
            throws ServiceException {
        logger.info("Updating passenger details for booking ID: {}", bookingId);
        
        // Validate inputs
        if (bookingId <= 0) {
            throw new ServiceException("Invalid booking ID");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ServiceException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ServiceException("Last name cannot be empty");
        }
        
        // Verify booking exists
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new ServiceException("Booking not found with ID: " + bookingId);
        }
        
        // Create JSON object with passenger details
        ObjectNode passengerDetails = objectMapper.createObjectNode();
        passengerDetails.put("firstName", firstName);
        passengerDetails.put("lastName", lastName);
        if (passportNumber != null && !passportNumber.trim().isEmpty()) {
            passengerDetails.put("passportNumber", passportNumber);
        }
        if (nationality != null && !nationality.trim().isEmpty()) {
            passengerDetails.put("nationality", nationality);
        }
        
        // Add special requirements as an array if provided
        if (specialRequirements != null && !specialRequirements.trim().isEmpty()) {
            String[] requirements = specialRequirements.split(",");
            passengerDetails.putArray("specialRequirements").addAll(
                    objectMapper.createArrayNode().addAll(
                            java.util.Arrays.stream(requirements)
                                    .map(r -> objectMapper.valueToTree(r.trim()))
                                    .collect(java.util.stream.Collectors.toList())));
        }
        
        // Update passenger details
        boolean success = bookingDAO.updatePassengerDetails(bookingId, passengerDetails);
        if (!success) {
            throw new ServiceException("Failed to update passenger details");
        }
        
        logger.debug("Passenger details updated successfully");
    }
    
    /**
     * Cancels a booking.
     * 
     * @param bookingId The booking ID
     * @throws ServiceException If validation fails or cancellation fails
     */
    public void cancelBooking(int bookingId) throws ServiceException {
        logger.info("Cancelling booking ID: {}", bookingId);
        
        // Validate inputs
        if (bookingId <= 0) {
            throw new ServiceException("Invalid booking ID");
        }
        
        // Verify booking exists
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new ServiceException("Booking not found with ID: " + bookingId);
        }
        
        // Cancel the booking
        boolean success = bookingDAO.cancelBooking(bookingId);
        if (!success) {
            throw new ServiceException("Failed to cancel booking (may be too close to departure)");
        }
        
        logger.debug("Booking cancelled successfully");
    }
    
    /**
     * Gets bookings for a client.
     * 
     * @param clientId The client ID
     * @return List of Booking objects
     * @throws ServiceException If validation fails or retrieval fails
     */
    public List<Booking> getClientBookings(int clientId) throws ServiceException {
        logger.info("Getting bookings for client ID: {}", clientId);
        
        // Validate inputs
        if (clientId <= 0) {
            throw new ServiceException("Invalid client ID");
        }
        
        // Verify client exists
        Client client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ServiceException("Client not found with ID: " + clientId);
        }
        
        // Get client bookings
        List<Booking> bookings = bookingDAO.getClientBookings(clientId);
        logger.debug("Retrieved {} bookings for client", bookings.size());
        
        return bookings;
    }
    
    /**
     * Gets details of a specific booking.
     * 
     * @param bookingId The booking ID
     * @return The Booking object
     * @throws ServiceException If validation fails or retrieval fails
     */
    public Booking getBookingDetails(int bookingId) throws ServiceException {
        logger.info("Getting details for booking ID: {}", bookingId);
        
        // Validate inputs
        if (bookingId <= 0) {
            throw new ServiceException("Invalid booking ID");
        }
        
        // Get booking details
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new ServiceException("Booking not found with ID: " + bookingId);
        }
        
        return booking;
    }
    
    /**
     * Redeems reward points for a discount on a booking.
     * 
     * @param clientId The client ID
     * @param pointsToRedeem The points to redeem
     * @param bookingId The booking ID
     * @return The discount amount
     * @throws ServiceException If validation fails or redemption fails
     */
    public double redeemPointsForDiscount(int clientId, int pointsToRedeem, int bookingId) throws ServiceException {
        logger.info("Redeeming {} points for client ID {} on booking ID {}", pointsToRedeem, clientId, bookingId);
        
        // Validate inputs
        if (clientId <= 0) {
            throw new ServiceException("Invalid client ID");
        }
        if (bookingId <= 0) {
            throw new ServiceException("Invalid booking ID");
        }
        if (pointsToRedeem <= 0) {
            throw new ServiceException("Points to redeem must be positive");
        }
        
        // Verify client exists and has enough points
        Client client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ServiceException("Client not found with ID: " + clientId);
        }
        int availablePoints = clientDAO.getClientRewardPoints(clientId);
        if (availablePoints < pointsToRedeem) {
            throw new ServiceException("Client does not have enough points (available: " + availablePoints + ")");
        }
        
        // Verify booking exists and belongs to client
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new ServiceException("Booking not found with ID: " + bookingId);
        }
        if (booking.getClientId() != clientId) {
            throw new ServiceException("Booking does not belong to this client");
        }
        
        // Redeem points
        double discountAmount = bookingDAO.redeemPointsForDiscount(clientId, pointsToRedeem, bookingId);
        if (discountAmount < 0) {
            throw new ServiceException("Failed to redeem points");
        }
        
        logger.debug("Points redeemed successfully for a discount of ${}", discountAmount);
        return discountAmount;
    }
} 