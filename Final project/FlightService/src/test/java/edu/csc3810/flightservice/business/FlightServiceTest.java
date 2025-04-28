package edu.csc3810.flightservice.business;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.csc3810.flightservice.dal.AirportDAO;
import edu.csc3810.flightservice.dal.FlightDAO;
import edu.csc3810.flightservice.model.Airport;
import edu.csc3810.flightservice.model.Flight;

/**
 * Unit tests for the FlightService class.
 */
public class FlightServiceTest {
    
    @Mock
    private FlightDAO flightDAO;
    
    @Mock
    private AirportDAO airportDAO;
    
    private FlightService flightService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        flightService = new FlightService(flightDAO, airportDAO);
    }
    
    @Test
    public void testSearchFlights_Success() throws ServiceException {
        // Arrange
        String originCode = "JFK";
        String destCode = "LAX";
        LocalDate departureDate = LocalDate.of(2023, 6, 15);
        
        Airport origin = new Airport(1, originCode, "John F. Kennedy International", "New York", "USA");
        Airport destination = new Airport(2, destCode, "Los Angeles International", "Los Angeles", "USA");
        
        List<Flight> expectedFlights = new ArrayList<>();
        Flight flight = new Flight();
        flight.setFlightId(1);
        flight.setFlightNumber("FL001");
        flight.setOriginAirportId(1);
        flight.setDestinationAirportId(2);
        flight.setDepartureDay(LocalDateTime.of(2023, 6, 15, 8, 0));
        flight.setArrivalDay(LocalDateTime.of(2023, 6, 15, 11, 30));
        flight.setBasePrice(BigDecimal.valueOf(350.00));
        flight.setStatus("Scheduled");
        flight.setOriginAirportCode(originCode);
        flight.setDestinationAirportCode(destCode);
        flight.setAvailableSeats(150);
        expectedFlights.add(flight);
        
        when(airportDAO.getAirportByCode(originCode)).thenReturn(origin);
        when(airportDAO.getAirportByCode(destCode)).thenReturn(destination);
        when(flightDAO.getAvailableFlights(originCode, destCode, departureDate)).thenReturn(expectedFlights);
        
        // Act
        List<Flight> actualFlights = flightService.searchFlights(originCode, destCode, departureDate);
        
        // Assert
        assertEquals(expectedFlights.size(), actualFlights.size());
        assertEquals(expectedFlights.get(0).getFlightId(), actualFlights.get(0).getFlightId());
        
        verify(airportDAO).getAirportByCode(originCode);
        verify(airportDAO).getAirportByCode(destCode);
        verify(flightDAO).getAvailableFlights(originCode, destCode, departureDate);
    }
    
    @Test
    public void testSearchFlights_EmptyOriginCode() {
        // Arrange
        String originCode = "";
        String destCode = "LAX";
        LocalDate departureDate = LocalDate.of(2023, 6, 15);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Origin airport code cannot be empty", exception.getMessage());
        
        verify(airportDAO, never()).getAirportByCode(anyString());
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_EmptyDestinationCode() {
        // Arrange
        String originCode = "JFK";
        String destCode = "";
        LocalDate departureDate = LocalDate.of(2023, 6, 15);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Destination airport code cannot be empty", exception.getMessage());
        
        verify(airportDAO, never()).getAirportByCode(anyString());
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_NullDepartureDate() {
        // Arrange
        String originCode = "JFK";
        String destCode = "LAX";
        LocalDate departureDate = null;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Departure date cannot be null", exception.getMessage());
        
        verify(airportDAO, never()).getAirportByCode(anyString());
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_PastDepartureDate() {
        // Arrange
        String originCode = "JFK";
        String destCode = "LAX";
        LocalDate departureDate = LocalDate.now().minusDays(1);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Departure date cannot be in the past", exception.getMessage());
        
        verify(airportDAO, never()).getAirportByCode(anyString());
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_SameOriginAndDestination() {
        // Arrange
        String originCode = "JFK";
        String destCode = "JFK";
        LocalDate departureDate = LocalDate.now().plusDays(1);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Origin and destination cannot be the same", exception.getMessage());
        
        verify(airportDAO, never()).getAirportByCode(anyString());
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_OriginAirportNotFound() {
        // Arrange
        String originCode = "XXX";
        String destCode = "LAX";
        LocalDate departureDate = LocalDate.now().plusDays(1);
        
        when(airportDAO.getAirportByCode(originCode)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Origin airport not found: XXX", exception.getMessage());
        
        verify(airportDAO).getAirportByCode(originCode);
        verify(airportDAO, never()).getAirportByCode(destCode);
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testSearchFlights_DestinationAirportNotFound() {
        // Arrange
        String originCode = "JFK";
        String destCode = "XXX";
        LocalDate departureDate = LocalDate.now().plusDays(1);
        
        Airport origin = new Airport(1, originCode, "John F. Kennedy International", "New York", "USA");
        
        when(airportDAO.getAirportByCode(originCode)).thenReturn(origin);
        when(airportDAO.getAirportByCode(destCode)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.searchFlights(originCode, destCode, departureDate);
        });
        
        assertEquals("Destination airport not found: XXX", exception.getMessage());
        
        verify(airportDAO).getAirportByCode(originCode);
        verify(airportDAO).getAirportByCode(destCode);
        verify(flightDAO, never()).getAvailableFlights(anyString(), anyString(), any(LocalDate.class));
    }
    
    @Test
    public void testGetFlightDetails_Success() throws ServiceException {
        // Arrange
        int flightId = 1;
        Flight expectedFlight = new Flight();
        expectedFlight.setFlightId(flightId);
        expectedFlight.setFlightNumber("FL001");
        
        when(flightDAO.getFlightById(flightId)).thenReturn(expectedFlight);
        
        // Act
        Flight actualFlight = flightService.getFlightDetails(flightId);
        
        // Assert
        assertNotNull(actualFlight);
        assertEquals(expectedFlight.getFlightId(), actualFlight.getFlightId());
        assertEquals(expectedFlight.getFlightNumber(), actualFlight.getFlightNumber());
        
        verify(flightDAO).getFlightById(flightId);
    }
    
    @Test
    public void testGetFlightDetails_InvalidId() {
        // Arrange
        int flightId = -1;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.getFlightDetails(flightId);
        });
        
        assertEquals("Invalid flight ID", exception.getMessage());
        
        verify(flightDAO, never()).getFlightById(anyInt());
    }
    
    @Test
    public void testGetFlightDetails_FlightNotFound() {
        // Arrange
        int flightId = 999;
        
        when(flightDAO.getFlightById(flightId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.getFlightDetails(flightId);
        });
        
        assertEquals("Flight not found with ID: 999", exception.getMessage());
        
        verify(flightDAO).getFlightById(flightId);
    }
    
    @Test
    public void testDelayFlight_Success() throws ServiceException {
        // Arrange
        int flightId = 1;
        int delayMinutes = 60;
        
        Flight flight = new Flight();
        flight.setFlightId(flightId);
        
        when(flightDAO.getFlightById(flightId)).thenReturn(flight);
        when(flightDAO.delayFlight(flightId, delayMinutes)).thenReturn(true);
        
        // Act
        flightService.delayFlight(flightId, delayMinutes);
        
        // Assert
        verify(flightDAO).getFlightById(flightId);
        verify(flightDAO).delayFlight(flightId, delayMinutes);
    }
    
    @Test
    public void testDelayFlight_InvalidFlightId() {
        // Arrange
        int flightId = -1;
        int delayMinutes = 60;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.delayFlight(flightId, delayMinutes);
        });
        
        assertEquals("Invalid flight ID", exception.getMessage());
        
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(flightDAO, never()).delayFlight(anyInt(), anyInt());
    }
    
    @Test
    public void testDelayFlight_InvalidDelayMinutes() {
        // Arrange
        int flightId = 1;
        int delayMinutes = 0;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.delayFlight(flightId, delayMinutes);
        });
        
        assertEquals("Delay minutes must be positive", exception.getMessage());
        
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(flightDAO, never()).delayFlight(anyInt(), anyInt());
    }
    
    @Test
    public void testDelayFlight_FlightNotFound() {
        // Arrange
        int flightId = 999;
        int delayMinutes = 60;
        
        when(flightDAO.getFlightById(flightId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.delayFlight(flightId, delayMinutes);
        });
        
        assertEquals("Flight not found with ID: 999", exception.getMessage());
        
        verify(flightDAO).getFlightById(flightId);
        verify(flightDAO, never()).delayFlight(anyInt(), anyInt());
    }
    
    @Test
    public void testDelayFlight_DelayFailed() {
        // Arrange
        int flightId = 1;
        int delayMinutes = 60;
        
        Flight flight = new Flight();
        flight.setFlightId(flightId);
        
        when(flightDAO.getFlightById(flightId)).thenReturn(flight);
        when(flightDAO.delayFlight(flightId, delayMinutes)).thenReturn(false);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            flightService.delayFlight(flightId, delayMinutes);
        });
        
        assertEquals("Failed to delay flight", exception.getMessage());
        
        verify(flightDAO).getFlightById(flightId);
        verify(flightDAO).delayFlight(flightId, delayMinutes);
    }
} 