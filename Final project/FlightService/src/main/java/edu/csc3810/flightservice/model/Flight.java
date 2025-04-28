package edu.csc3810.flightservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a flight in the system.
 */
public class Flight {
    private int flightId;
    private String flightNumber;
    private int planeId;
    private int originAirportId;
    private int destinationAirportId;
    private LocalDateTime departureDay;
    private LocalDateTime arrivalDay;
    private BigDecimal basePrice;
    private String status; // Scheduled, Delayed, Cancelled
    
    // Non-database fields for convenience
    private String originAirportCode;
    private String originCity;
    private String destinationAirportCode;
    private String destinationCity;
    private int availableSeats;

    /**
     * Default constructor
     */
    public Flight() {
    }

    /**
     * Fully parameterized constructor for database fields
     */
    public Flight(int flightId, String flightNumber, int planeId, int originAirportId, int destinationAirportId,
                  LocalDateTime departureDay, LocalDateTime arrivalDay, BigDecimal basePrice, String status) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.planeId = planeId;
        this.originAirportId = originAirportId;
        this.destinationAirportId = destinationAirportId;
        this.departureDay = departureDay;
        this.arrivalDay = arrivalDay;
        this.basePrice = basePrice;
        this.status = status;
    }

    // Getters and Setters
    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public int getOriginAirportId() {
        return originAirportId;
    }

    public void setOriginAirportId(int originAirportId) {
        this.originAirportId = originAirportId;
    }

    public int getDestinationAirportId() {
        return destinationAirportId;
    }

    public void setDestinationAirportId(int destinationAirportId) {
        this.destinationAirportId = destinationAirportId;
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

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOriginAirportCode() {
        return originAirportCode;
    }

    public void setOriginAirportCode(String originAirportCode) {
        this.originAirportCode = originAirportCode;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    public void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    @Override
    public String toString() {
        return flightNumber + ": " + originAirportCode + "(" + originCity + ") → " 
               + destinationAirportCode + "(" + destinationCity + ") - " 
               + departureDay + " - $" + basePrice;
    }
} 