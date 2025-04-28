package edu.csc3810.flightservice.dal;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.model.Flight;

/**
 * Data access object for Flight entity.
 */
public class FlightDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightDAO.class);
    
    /**
     * Retrieves available flights using the stored procedure.
     * 
     * @param originCode The origin airport code
     * @param destCode The destination airport code
     * @param departureDate The departure date
     * @return List of Flight objects
     */
    public List<Flight> getAvailableFlights(String originCode, String destCode, LocalDate departureDate) {
        logger.info("Getting available flights from {} to {} on {}", originCode, destCode, departureDate);
        List<Flight> flights = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL GetAvailableFlights(?, ?, ?)}");
            stmt.setString(1, originCode);
            stmt.setString(2, destCode);
            stmt.setObject(3, departureDate);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Flight flight = new Flight();
                flight.setFlightId(rs.getInt("FlightId"));
                flight.setFlightNumber(rs.getString("FlightNumber"));
                flight.setOriginAirportCode(rs.getString("OriginCode"));
                flight.setOriginCity(rs.getString("OriginCity"));
                flight.setDestinationAirportCode(rs.getString("DestinationCode"));
                flight.setDestinationCity(rs.getString("DestinationCity"));
                flight.setDepartureDay(rs.getObject("DepartureDay", LocalDateTime.class));
                flight.setArrivalDay(rs.getObject("ArrivalDay", LocalDateTime.class));
                flight.setBasePrice(rs.getBigDecimal("BasePrice"));
                flight.setAvailableSeats(rs.getInt("AvailableSeats"));
                flights.add(flight);
            }
            
            logger.debug("Found {} available flights", flights.size());
        } catch (SQLException e) {
            logger.error("Error retrieving available flights", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return flights;
    }
    
    /**
     * Gets a flight by ID.
     * 
     * @param flightId The flight ID
     * @return The Flight object, or null if not found
     */
    public Flight getFlightById(int flightId) {
        logger.info("Getting flight with ID: {}", flightId);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Flight flight = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT f.FlightId, f.FlightNumber, f.PlaneId, f.OriginAirportId, f.DestinationAirportId, " +
                          "f.DepartureDay, f.ArrivalDay, f.BasePrice, f.STATUS, " +
                          "o.AirportCode as OriginCode, o.City as OriginCity, " +
                          "d.AirportCode as DestinationCode, d.City as DestinationCity " +
                          "FROM Flights f " +
                          "JOIN Airports o ON f.OriginAirportId = o.AirportId " +
                          "JOIN Airports d ON f.DestinationAirportId = d.AirportId " +
                          "WHERE f.FlightId = ?";
            stmt = conn.prepareCall(query);
            stmt.setInt(1, flightId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                flight = new Flight();
                flight.setFlightId(rs.getInt("FlightId"));
                flight.setFlightNumber(rs.getString("FlightNumber"));
                flight.setPlaneId(rs.getInt("PlaneId"));
                flight.setOriginAirportId(rs.getInt("OriginAirportId"));
                flight.setDestinationAirportId(rs.getInt("DestinationAirportId"));
                flight.setDepartureDay(rs.getObject("DepartureDay", LocalDateTime.class));
                flight.setArrivalDay(rs.getObject("ArrivalDay", LocalDateTime.class));
                flight.setBasePrice(rs.getBigDecimal("BasePrice"));
                flight.setStatus(rs.getString("STATUS"));
                flight.setOriginAirportCode(rs.getString("OriginCode"));
                flight.setOriginCity(rs.getString("OriginCity"));
                flight.setDestinationAirportCode(rs.getString("DestinationCode"));
                flight.setDestinationCity(rs.getString("DestinationCity"));
                
                logger.debug("Found flight: {}", flight);
            } else {
                logger.debug("No flight found with ID: {}", flightId);
            }
        } catch (SQLException e) {
            logger.error("Error getting flight by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return flight;
    }
    
    /**
     * Delays a flight using the stored procedure.
     * 
     * @param flightId The flight ID
     * @param delayMinutes The delay in minutes
     * @return true if successful, false otherwise
     */
    public boolean delayFlight(int flightId, int delayMinutes) {
        logger.info("Delaying flight ID {} by {} minutes", flightId, delayMinutes);
        Connection conn = null;
        CallableStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL DelayFlight(?, ?)}");
            stmt.setInt(1, flightId);
            stmt.setInt(2, delayMinutes);
            stmt.execute();
            success = true;
            logger.debug("Flight delayed successfully");
        } catch (SQLException e) {
            logger.error("Error delaying flight", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return success;
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