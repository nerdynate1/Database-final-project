package edu.csc3810.flightservice.business;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.dal.AirportDAO;
import edu.csc3810.flightservice.dal.FlightDAO;
import edu.csc3810.flightservice.model.Airport;
import edu.csc3810.flightservice.model.Flight;

/**
 * Business logic for flight operations.
 */
public class FlightService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);
    
    private final FlightDAO flightDAO;
    private final AirportDAO airportDAO;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param flightDAO The flight data access object
     * @param airportDAO The airport data access object
     */
    public FlightService(FlightDAO flightDAO, AirportDAO airportDAO) {
        this.flightDAO = flightDAO;
        this.airportDAO = airportDAO;
    }
    
    /**
     * Searches for flights based on criteria.
     * 
     * @param originCode The origin airport code
     * @param destinationCode The destination airport code
     * @param departureDate The departure date
     * @return List of Flight objects
     * @throws ServiceException If validation fails or a database error occurs
     */
    public List<Flight> searchFlights(String originCode, String destinationCode, LocalDate departureDate) throws ServiceException {
        logger.info("Searching flights from {} to {} on {}", originCode, destinationCode, departureDate);
        
        // Validate inputs
        if (originCode == null || originCode.trim().isEmpty()) {
            throw new ServiceException("Origin airport code cannot be empty");
        }
        if (destinationCode == null || destinationCode.trim().isEmpty()) {
            throw new ServiceException("Destination airport code cannot be empty");
        }
        if (departureDate == null) {
            throw new ServiceException("Departure date cannot be null");
        }
        if (departureDate.isBefore(LocalDate.now())) {
            throw new ServiceException("Departure date cannot be in the past");
        }
        if (originCode.equals(destinationCode)) {
            throw new ServiceException("Origin and destination cannot be the same");
        }
        
        // Verify airports exist
        Airport origin = airportDAO.getAirportByCode(originCode);
        if (origin == null) {
            throw new ServiceException("Origin airport not found: " + originCode);
        }
        
        Airport destination = airportDAO.getAirportByCode(destinationCode);
        if (destination == null) {
            throw new ServiceException("Destination airport not found: " + destinationCode);
        }
        
        // Search for flights
        List<Flight> flights = flightDAO.getAvailableFlights(originCode, destinationCode, departureDate);
        logger.debug("Found {} flights matching criteria", flights.size());
        
        return flights;
    }
    
    /**
     * Gets details of a specific flight.
     * 
     * @param flightId The flight ID
     * @return The Flight object
     * @throws ServiceException If the flight is not found or a database error occurs
     */
    public Flight getFlightDetails(int flightId) throws ServiceException {
        logger.info("Getting details for flight ID: {}", flightId);
        
        if (flightId <= 0) {
            throw new ServiceException("Invalid flight ID");
        }
        
        Flight flight = flightDAO.getFlightById(flightId);
        if (flight == null) {
            throw new ServiceException("Flight not found with ID: " + flightId);
        }
        
        return flight;
    }
    
    /**
     * Delays a flight by a specified number of minutes.
     * 
     * @param flightId The flight ID
     * @param delayMinutes The delay in minutes
     * @throws ServiceException If the delay fails or validation fails
     */
    public void delayFlight(int flightId, int delayMinutes) throws ServiceException {
        logger.info("Delaying flight ID {} by {} minutes", flightId, delayMinutes);
        
        // Validate inputs
        if (flightId <= 0) {
            throw new ServiceException("Invalid flight ID");
        }
        if (delayMinutes <= 0) {
            throw new ServiceException("Delay minutes must be positive");
        }
        
        // Verify flight exists
        Flight flight = flightDAO.getFlightById(flightId);
        if (flight == null) {
            throw new ServiceException("Flight not found with ID: " + flightId);
        }
        
        // Apply the delay
        boolean success = flightDAO.delayFlight(flightId, delayMinutes);
        if (!success) {
            throw new ServiceException("Failed to delay flight");
        }
        
        logger.debug("Flight delayed successfully");
    }
} 