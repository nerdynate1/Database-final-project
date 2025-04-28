package edu.csc3810.flightservice.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.csc3810.flightservice.model.Booking;

/**
 * Data access object for Booking entity.
 */
public class BookingDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingDAO.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Books a flight using the stored procedure.
     * 
     * @param clientId The client ID
     * @param flightId The flight ID
     * @param seatNumber The seat number
     * @return The booking ID if successful, -1 otherwise
     */
    public int bookFlight(int clientId, int flightId, String seatNumber) {
        logger.info("Booking flight ID {} for client ID {} with seat {}", flightId, clientId, seatNumber);
        Connection conn = null;
        CallableStatement stmt = null;
        int bookingId = -1;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL BookFlight(?, ?, ?, ?)}");
            stmt.setInt(1, clientId);
            stmt.setInt(2, flightId);
            stmt.setString(3, seatNumber);
            stmt.registerOutParameter(4, Types.INTEGER);
            stmt.execute();
            
            bookingId = stmt.getInt(4);
            logger.debug("Flight booked successfully with booking ID: {}", bookingId);
        } catch (SQLException e) {
            logger.error("Error booking flight", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return bookingId;
    }
    
    /**
     * Updates passenger details using the stored procedure.
     * 
     * @param bookingId The booking ID
     * @param passengerDetails The passenger details as JSON
     * @return true if successful, false otherwise
     */
    public boolean updatePassengerDetails(int bookingId, JsonNode passengerDetails) {
        logger.info("Updating passenger details for booking ID: {}", bookingId);
        Connection conn = null;
        CallableStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL UpdatePassengerDetails(?, ?)}");
            stmt.setInt(1, bookingId);
            stmt.setString(2, passengerDetails.toString());
            stmt.execute();
            success = true;
            logger.debug("Passenger details updated successfully");
        } catch (SQLException e) {
            logger.error("Error updating passenger details", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return success;
    }
    
    /**
     * Cancels a booking using the stored procedure.
     * 
     * @param bookingId The booking ID
     * @return true if successful, false otherwise
     */
    public boolean cancelBooking(int bookingId) {
        logger.info("Cancelling booking ID: {}", bookingId);
        Connection conn = null;
        CallableStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL CancelBooking(?, ?)}");
            stmt.setInt(1, bookingId);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.execute();
            
            success = stmt.getBoolean(2);
            logger.debug("Booking cancellation result: {}", success ? "Success" : "Failed");
        } catch (SQLException e) {
            logger.error("Error cancelling booking", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return success;
    }
    
    /**
     * Gets bookings for a client using the stored procedure.
     * 
     * @param clientId The client ID
     * @return List of Booking objects
     */
    public List<Booking> getClientBookings(int clientId) {
        logger.info("Getting bookings for client ID: {}", clientId);
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL GetClientBookings(?)}");
            stmt.setInt(1, clientId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("BookingId"));
                booking.setFlightNumber(rs.getString("FlightNumber"));
                booking.setOriginAirportCode(rs.getString("Origin"));
                booking.setDestinationAirportCode(rs.getString("Destination"));
                booking.setDepartureDay(rs.getObject("DepartureDay", LocalDateTime.class));
                booking.setArrivalDay(rs.getObject("ArrivalDay", LocalDateTime.class));
                booking.setSeatNumber(rs.getString("SeatNumber"));
                booking.setStatus(rs.getString("BookingStatus"));
                
                // Manually set the remaining fields
                booking.setClientId(clientId);
                booking.setFlightId(rs.getInt("FlightId"));
                
                // Parse JSON data if available
                String jsonData = rs.getString("PassengerDetails");
                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        booking.setPassengerDetails(objectMapper.readTree(jsonData));
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing passenger details JSON", e);
                    }
                }
                
                bookings.add(booking);
            }
            
            logger.debug("Retrieved {} bookings", bookings.size());
        } catch (SQLException e) {
            logger.error("Error retrieving client bookings", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return bookings;
    }
    
    /**
     * Gets a booking by ID.
     * 
     * @param bookingId The booking ID
     * @return The Booking object, or null if not found
     */
    public Booking getBookingById(int bookingId) {
        logger.info("Getting booking with ID: {}", bookingId);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Booking booking = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT b.BookingId, b.ClientId, b.FlightId, b.BookingDate, b.SeatNumber, b.STATUS, " +
                          "b.PassengerDetails, c.ClientsName, f.FlightNumber, " +
                          "o.AirportCode as Origin, d.AirportCode as Destination, " +
                          "f.DepartureDay, f.ArrivalDay " +
                          "FROM Bookings b " +
                          "JOIN Clients c ON b.ClientId = c.ClientId " +
                          "JOIN Flights f ON b.FlightId = f.FlightId " +
                          "JOIN Airports o ON f.OriginAirportId = o.AirportId " +
                          "JOIN Airports d ON f.DestinationAirportId = d.AirportId " +
                          "WHERE b.BookingId = ?";
            stmt = conn.prepareCall(query);
            stmt.setInt(1, bookingId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                booking = new Booking();
                booking.setBookingId(rs.getInt("BookingId"));
                booking.setClientId(rs.getInt("ClientId"));
                booking.setFlightId(rs.getInt("FlightId"));
                booking.setBookingDate(rs.getObject("BookingDate", LocalDateTime.class));
                booking.setSeatNumber(rs.getString("SeatNumber"));
                booking.setStatus(rs.getString("STATUS"));
                booking.setClientName(rs.getString("ClientsName"));
                booking.setFlightNumber(rs.getString("FlightNumber"));
                booking.setOriginAirportCode(rs.getString("Origin"));
                booking.setDestinationAirportCode(rs.getString("Destination"));
                booking.setDepartureDay(rs.getObject("DepartureDay", LocalDateTime.class));
                booking.setArrivalDay(rs.getObject("ArrivalDay", LocalDateTime.class));
                
                // Parse JSON data if available
                String jsonData = rs.getString("PassengerDetails");
                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        booking.setPassengerDetails(objectMapper.readTree(jsonData));
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing passenger details JSON", e);
                    }
                }
                
                logger.debug("Found booking: {}", booking);
            } else {
                logger.debug("No booking found with ID: {}", bookingId);
            }
        } catch (SQLException e) {
            logger.error("Error getting booking by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return booking;
    }
    
    /**
     * Redeems points for a discount on a booking.
     * 
     * @param clientId The client ID
     * @param pointsToRedeem The number of points to redeem
     * @param bookingId The booking ID
     * @return The discount amount, or -1 if failed
     */
    public double redeemPointsForDiscount(int clientId, int pointsToRedeem, int bookingId) {
        logger.info("Redeeming {} points for client ID {} on booking ID {}", pointsToRedeem, clientId, bookingId);
        Connection conn = null;
        CallableStatement stmt = null;
        double discountAmount = -1;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL RedeemPointsForDiscount(?, ?, ?, ?, ?)}");
            stmt.setInt(1, clientId);
            stmt.setInt(2, pointsToRedeem);
            stmt.setInt(3, bookingId);
            stmt.registerOutParameter(4, Types.DECIMAL);
            stmt.registerOutParameter(5, Types.INTEGER);
            stmt.execute();
            
            discountAmount = stmt.getDouble(4);
            int remainingPoints = stmt.getInt(5);
            logger.debug("Points redeemed successfully. Discount: {}, Remaining points: {}", discountAmount, remainingPoints);
        } catch (SQLException e) {
            logger.error("Error redeeming points", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return discountAmount;
    }
    
    /**
     * Safely closes database resources.
     */
    private void closeResources(Connection conn, CallableStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Error closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing statement", e);
            }
        }
        DatabaseConnector.closeConnection(conn);
    }
} 