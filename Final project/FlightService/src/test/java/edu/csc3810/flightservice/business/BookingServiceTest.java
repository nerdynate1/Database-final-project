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
 * Unit tests for the BookingService class.
 */
public class BookingServiceTest {
    
    @Mock
    private BookingDAO bookingDAO;
    
    @Mock
    private ClientDAO clientDAO;
    
    @Mock
    private FlightDAO flightDAO;
    
    private BookingService bookingService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService(bookingDAO, clientDAO, flightDAO);
        objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testBookFlight_Success() throws ServiceException {
        // Arrange
        int clientId = 1;
        int flightId = 1;
        String seatNumber = "12A";
        int expectedBookingId = 10;
        
        Client client = new Client(clientId, "John Smith", "john@example.com", LocalDate.now().minusYears(1), 100);
        Flight flight = new Flight(flightId, "FL001", 1, 1, 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3), BigDecimal.valueOf(350.00), "Scheduled");
        
        when(clientDAO.getClientById(clientId)).thenReturn(client);
        when(flightDAO.getFlightById(flightId)).thenReturn(flight);
        when(bookingDAO.bookFlight(clientId, flightId, seatNumber)).thenReturn(expectedBookingId);
        
        // Act
        int actualBookingId = bookingService.bookFlight(clientId, flightId, seatNumber);
        
        // Assert
        assertEquals(expectedBookingId, actualBookingId);
        
        verify(clientDAO).getClientById(clientId);
        verify(flightDAO).getFlightById(flightId);
        verify(bookingDAO).bookFlight(clientId, flightId, seatNumber);
    }
    
    @Test
    public void testBookFlight_InvalidClientId() {
        // Arrange
        int clientId = -1;
        int flightId = 1;
        String seatNumber = "12A";
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Invalid client ID", exception.getMessage());
        
        verify(clientDAO, never()).getClientById(anyInt());
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(bookingDAO, never()).bookFlight(anyInt(), anyInt(), anyString());
    }
    
    @Test
    public void testBookFlight_InvalidFlightId() {
        // Arrange
        int clientId = 1;
        int flightId = -1;
        String seatNumber = "12A";
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Invalid flight ID", exception.getMessage());
        
        verify(clientDAO, never()).getClientById(anyInt());
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(bookingDAO, never()).bookFlight(anyInt(), anyInt(), anyString());
    }
    
    @Test
    public void testBookFlight_InvalidSeatNumber() {
        // Arrange
        int clientId = 1;
        int flightId = 1;
        String seatNumber = "X12";
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Invalid seat number format (should be like 1A, 12F)", exception.getMessage());
        
        verify(clientDAO, never()).getClientById(anyInt());
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(bookingDAO, never()).bookFlight(anyInt(), anyInt(), anyString());
    }
    
    @Test
    public void testBookFlight_ClientNotFound() {
        // Arrange
        int clientId = 999;
        int flightId = 1;
        String seatNumber = "12A";
        
        when(clientDAO.getClientById(clientId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Client not found with ID: 999", exception.getMessage());
        
        verify(clientDAO).getClientById(clientId);
        verify(flightDAO, never()).getFlightById(anyInt());
        verify(bookingDAO, never()).bookFlight(anyInt(), anyInt(), anyString());
    }
    
    @Test
    public void testBookFlight_FlightNotFound() {
        // Arrange
        int clientId = 1;
        int flightId = 999;
        String seatNumber = "12A";
        
        Client client = new Client(clientId, "John Smith", "john@example.com", LocalDate.now().minusYears(1), 100);
        
        when(clientDAO.getClientById(clientId)).thenReturn(client);
        when(flightDAO.getFlightById(flightId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Flight not found with ID: 999", exception.getMessage());
        
        verify(clientDAO).getClientById(clientId);
        verify(flightDAO).getFlightById(flightId);
        verify(bookingDAO, never()).bookFlight(anyInt(), anyInt(), anyString());
    }
    
    @Test
    public void testBookFlight_BookingFailed() {
        // Arrange
        int clientId = 1;
        int flightId = 1;
        String seatNumber = "12A";
        
        Client client = new Client(clientId, "John Smith", "john@example.com", LocalDate.now().minusYears(1), 100);
        Flight flight = new Flight(flightId, "FL001", 1, 1, 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3), BigDecimal.valueOf(350.00), "Scheduled");
        
        when(clientDAO.getClientById(clientId)).thenReturn(client);
        when(flightDAO.getFlightById(flightId)).thenReturn(flight);
        when(bookingDAO.bookFlight(clientId, flightId, seatNumber)).thenReturn(-1);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.bookFlight(clientId, flightId, seatNumber);
        });
        
        assertEquals("Flight booking failed", exception.getMessage());
        
        verify(clientDAO).getClientById(clientId);
        verify(flightDAO).getFlightById(flightId);
        verify(bookingDAO).bookFlight(clientId, flightId, seatNumber);
    }
    
    @Test
    public void testUpdatePassengerDetails_Success() throws ServiceException, Exception {
        // Arrange
        int bookingId = 1;
        String firstName = "John";
        String lastName = "Smith";
        String passportNumber = "AB1234567";
        String nationality = "USA";
        String specialRequirements = "Vegetarian meal, Window seat";
        
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        
        when(bookingDAO.getBookingById(bookingId)).thenReturn(booking);
        when(bookingDAO.updatePassengerDetails(eq(bookingId), any(JsonNode.class))).thenReturn(true);
        
        // Act
        bookingService.updatePassengerDetails(bookingId, firstName, lastName, passportNumber, nationality, specialRequirements);
        
        // Assert
        verify(bookingDAO).getBookingById(bookingId);
        verify(bookingDAO).updatePassengerDetails(eq(bookingId), any(JsonNode.class));
    }
    
    @Test
    public void testUpdatePassengerDetails_InvalidBookingId() {
        // Arrange
        int bookingId = -1;
        String firstName = "John";
        String lastName = "Smith";
        String passportNumber = "AB1234567";
        String nationality = "USA";
        String specialRequirements = "Vegetarian meal, Window seat";
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.updatePassengerDetails(bookingId, firstName, lastName, passportNumber, nationality, specialRequirements);
        });
        
        assertEquals("Invalid booking ID", exception.getMessage());
        
        verify(bookingDAO, never()).getBookingById(anyInt());
        verify(bookingDAO, never()).updatePassengerDetails(anyInt(), any(JsonNode.class));
    }
    
    @Test
    public void testCancelBooking_Success() throws ServiceException {
        // Arrange
        int bookingId = 1;
        
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        
        when(bookingDAO.getBookingById(bookingId)).thenReturn(booking);
        when(bookingDAO.cancelBooking(bookingId)).thenReturn(true);
        
        // Act
        bookingService.cancelBooking(bookingId);
        
        // Assert
        verify(bookingDAO).getBookingById(bookingId);
        verify(bookingDAO).cancelBooking(bookingId);
    }
    
    @Test
    public void testCancelBooking_InvalidBookingId() {
        // Arrange
        int bookingId = -1;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.cancelBooking(bookingId);
        });
        
        assertEquals("Invalid booking ID", exception.getMessage());
        
        verify(bookingDAO, never()).getBookingById(anyInt());
        verify(bookingDAO, never()).cancelBooking(anyInt());
    }
    
    @Test
    public void testCancelBooking_BookingNotFound() {
        // Arrange
        int bookingId = 999;
        
        when(bookingDAO.getBookingById(bookingId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.cancelBooking(bookingId);
        });
        
        assertEquals("Booking not found with ID: 999", exception.getMessage());
        
        verify(bookingDAO).getBookingById(bookingId);
        verify(bookingDAO, never()).cancelBooking(anyInt());
    }
    
    @Test
    public void testCancelBooking_CancellationFailed() {
        // Arrange
        int bookingId = 1;
        
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        
        when(bookingDAO.getBookingById(bookingId)).thenReturn(booking);
        when(bookingDAO.cancelBooking(bookingId)).thenReturn(false);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.cancelBooking(bookingId);
        });
        
        assertEquals("Failed to cancel booking (may be too close to departure)", exception.getMessage());
        
        verify(bookingDAO).getBookingById(bookingId);
        verify(bookingDAO).cancelBooking(bookingId);
    }
    
    @Test
    public void testGetClientBookings_Success() throws ServiceException {
        // Arrange
        int clientId = 1;
        
        Client client = new Client(clientId, "John Smith", "john@example.com", LocalDate.now().minusYears(1), 100);
        List<Booking> expectedBookings = new ArrayList<>();
        expectedBookings.add(new Booking());
        
        when(clientDAO.getClientById(clientId)).thenReturn(client);
        when(bookingDAO.getClientBookings(clientId)).thenReturn(expectedBookings);
        
        // Act
        List<Booking> actualBookings = bookingService.getClientBookings(clientId);
        
        // Assert
        assertEquals(expectedBookings.size(), actualBookings.size());
        
        verify(clientDAO).getClientById(clientId);
        verify(bookingDAO).getClientBookings(clientId);
    }
    
    @Test
    public void testGetClientBookings_InvalidClientId() {
        // Arrange
        int clientId = -1;
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.getClientBookings(clientId);
        });
        
        assertEquals("Invalid client ID", exception.getMessage());
        
        verify(clientDAO, never()).getClientById(anyInt());
        verify(bookingDAO, never()).getClientBookings(anyInt());
    }
    
    @Test
    public void testGetClientBookings_ClientNotFound() {
        // Arrange
        int clientId = 999;
        
        when(clientDAO.getClientById(clientId)).thenReturn(null);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bookingService.getClientBookings(clientId);
        });
        
        assertEquals("Client not found with ID: 999", exception.getMessage());
        
        verify(clientDAO).getClientById(clientId);
        verify(bookingDAO, never()).getClientBookings(anyInt());
    }
} 