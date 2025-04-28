package edu.csc3810.flightservice.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a booking in the system.
 */
public class Booking {
    private int bookingId;
    private int clientId;
    private int flightId;
    private LocalDateTime bookingDate;
    private String seatNumber;
    private String status; // Confirmed, Cancelled
    private JsonNode passengerDetails; // JSON data
    
    // Non-database fields for convenience
    private String clientName;
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureDay;
    private LocalDateTime arrivalDay;

    /**
     * Default constructor
     */
    public Booking() {
    }

    /**
     * Fully parameterized constructor for database fields
     */
    public Booking(int bookingId, int clientId, int flightId, LocalDateTime bookingDate, 
                   String seatNumber, String status, JsonNode passengerDetails) {
        this.bookingId = bookingId;
        this.clientId = clientId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.seatNumber = seatNumber;
        this.status = status;
        this.passengerDetails = passengerDetails;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonNode getPassengerDetails() {
        return passengerDetails;
    }

    public void setPassengerDetails(JsonNode passengerDetails) {
        this.passengerDetails = passengerDetails;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOriginAirportCode() {
        return originAirportCode;
    }

    public void setOriginAirportCode(String originAirportCode) {
        this.originAirportCode = originAirportCode;
    }

    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    public void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public LocalDateTime getDepartureDay() {
        return departureDay;
    }

    public void setDepartureDay(LocalDateTime departureDay) {
        this.departureDay = departureDay;
    }

    public LocalDateTime getArrivalDay() {
        return arrivalDay;
    }

    public void setArrivalDay(LocalDateTime arrivalDay) {
        this.arrivalDay = arrivalDay;
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + ": " + flightNumber + " - " + originAirportCode + " to " + 
               destinationAirportCode + " - Seat: " + seatNumber + " - Status: " + status;
    }
} 