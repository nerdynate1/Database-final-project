package edu.csc3810.flightservice.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.model.Airport;

/**
 * Data access object for Airport entity.
 */
public class AirportDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(AirportDAO.class);
    
    /**
     * Retrieves all airports from the database.
     * 
     * @return List of Airport objects
     */
    public List<Airport> getAllAirports() {
        logger.info("Getting all airports");
        List<Airport> airports = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT AirportId, AirportCode, AirportName, City, Country FROM Airports ORDER BY AirportCode";
            stmt = conn.prepareCall(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Airport airport = new Airport();
                airport.setAirportId(rs.getInt("AirportId"));
                airport.setAirportCode(rs.getString("AirportCode"));
                airport.setAirportName(rs.getString("AirportName"));
                airport.setCity(rs.getString("City"));
                airport.setCountry(rs.getString("Country"));
                airports.add(airport);
            }
            
            logger.debug("Retrieved {} airports", airports.size());
        } catch (SQLException e) {
            logger.error("Error retrieving airports", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return airports;
    }
    
    /**
     * Searches for airports using the stored procedure.
     * 
     * @param searchTerm The search term
     * @return List of Airport objects matching the search
     */
    public List<Airport> searchAirports(String searchTerm) {
        logger.info("Searching airports with term: {}", searchTerm);
        List<Airport> airports = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareCall("{CALL SearchAirports(?)}");
            stmt.setString(1, searchTerm);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Airport airport = new Airport();
                airport.setAirportId(rs.getInt("AirportId"));
                airport.setAirportCode(rs.getString("AirportCode"));
                airport.setAirportName(rs.getString("AirportName"));
                airport.setCity(rs.getString("City"));
                airport.setCountry(rs.getString("Country"));
                airports.add(airport);
            }
            
            logger.debug("Search found {} airports", airports.size());
        } catch (SQLException e) {
            logger.error("Error searching airports", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return airports;
    }
    
    /**
     * Gets an airport by ID.
     * 
     * @param airportId The airport ID
     * @return The Airport object, or null if not found
     */
    public Airport getAirportById(int airportId) {
        logger.info("Getting airport with ID: {}", airportId);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Airport airport = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT AirportId, AirportCode, AirportName, City, Country FROM Airports WHERE AirportId = ?";
            stmt = conn.prepareCall(query);
            stmt.setInt(1, airportId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                airport = new Airport();
                airport.setAirportId(rs.getInt("AirportId"));
                airport.setAirportCode(rs.getString("AirportCode"));
                airport.setAirportName(rs.getString("AirportName"));
                airport.setCity(rs.getString("City"));
                airport.setCountry(rs.getString("Country"));
                logger.debug("Found airport: {}", airport);
            } else {
                logger.debug("No airport found with ID: {}", airportId);
            }
        } catch (SQLException e) {
            logger.error("Error getting airport by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return airport;
    }
    
    /**
     * Gets an airport by its code.
     * 
     * @param airportCode The airport code (3-letter IATA code)
     * @return The Airport object, or null if not found
     */
    public Airport getAirportByCode(String airportCode) {
        logger.info("Getting airport with code: {}", airportCode);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Airport airport = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT AirportId, AirportCode, AirportName, City, Country FROM Airports WHERE AirportCode = ?";
            stmt = conn.prepareCall(query);
            stmt.setString(1, airportCode);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                airport = new Airport();
                airport.setAirportId(rs.getInt("AirportId"));
                airport.setAirportCode(rs.getString("AirportCode"));
                airport.setAirportName(rs.getString("AirportName"));
                airport.setCity(rs.getString("City"));
                airport.setCountry(rs.getString("Country"));
                logger.debug("Found airport: {}", airport);
            } else {
                logger.debug("No airport found with code: {}", airportCode);
            }
        } catch (SQLException e) {
            logger.error("Error getting airport by code", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return airport;
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