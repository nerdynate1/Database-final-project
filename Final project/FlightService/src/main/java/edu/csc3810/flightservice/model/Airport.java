package edu.csc3810.flightservice.model;

/**
 * Represents an airport in the system.
 */
public class Airport {
    private int airportId;
    private String airportCode;
    private String airportName;
    private String city;
    private String country;

    /**
     * Default constructor
     */
    public Airport() {
    }

    /**
     * Fully parameterized constructor
     * 
     * @param airportId The airport ID
     * @param airportCode The airport IATA code (3-letter)
     * @param airportName The full name of the airport
     * @param city The city where the airport is located
     * @param country The country where the airport is located
     */
    public Airport(int airportId, String airportCode, String airportName, String city, String country) {
        this.airportId = airportId;
        this.airportCode = airportCode;
        this.airportName = airportName;
        this.city = city;
        this.country = country;
    }

    // Getters and Setters
    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return airportCode + " - " + airportName + " (" + city + ", " + country + ")";
    }
} 