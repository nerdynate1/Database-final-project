package edu.csc3810.flightservice.model;

import java.time.LocalDate;

/**
 * Represents a client in the system.
 */
public class Client {
    private int clientId;
    private String clientName;
    private String email;
    private LocalDate dateOfSubscription;
    private int rewardPoints;

    /**
     * Default constructor
     */
    public Client() {
    }

    /**
     * Fully parameterized constructor
     * 
     * @param clientId The client ID
     * @param clientName The client's full name
     * @param email The client's email address
     * @param dateOfSubscription The date when the client subscribed
     * @param rewardPoints The current reward points balance
     */
    public Client(int clientId, String clientName, String email, LocalDate dateOfSubscription, int rewardPoints) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.dateOfSubscription = dateOfSubscription;
        this.rewardPoints = rewardPoints;
    }

    // Getters and Setters
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfSubscription() {
        return dateOfSubscription;
    }

    public void setDateOfSubscription(LocalDate dateOfSubscription) {
        this.dateOfSubscription = dateOfSubscription;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    @Override
    public String toString() {
        return clientId + " - " + clientName + " (" + email + ")";
    }
} 