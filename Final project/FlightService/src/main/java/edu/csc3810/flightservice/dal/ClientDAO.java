package edu.csc3810.flightservice.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.model.Client;

/**
 * Data access object for Client entity.
 */
public class ClientDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientDAO.class);
    
    /**
     * Retrieves all clients from the database.
     * 
     * @return List of Client objects
     */
    public List<Client> getAllClients() {
        logger.info("Getting all clients");
        List<Client> clients = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT c.ClientId, c.ClientsName, c.Email, c.DateofSubscription, " +
                          "IFNULL(SUM(r.PointsEarned) - SUM(r.PointsUsed), 0) AS RewardPoints " +
                          "FROM Clients c " +
                          "LEFT JOIN Rewards r ON c.ClientId = r.ClientId " +
                          "GROUP BY c.ClientId, c.ClientsName, c.Email, c.DateofSubscription " +
                          "ORDER BY c.ClientsName";
            stmt = conn.prepareCall(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Client client = new Client();
                client.setClientId(rs.getInt("ClientId"));
                client.setClientName(rs.getString("ClientsName"));
                client.setEmail(rs.getString("Email"));
                client.setDateOfSubscription(rs.getObject("DateofSubscription", LocalDate.class));
                client.setRewardPoints(rs.getInt("RewardPoints"));
                clients.add(client);
            }
            
            logger.debug("Retrieved {} clients", clients.size());
        } catch (SQLException e) {
            logger.error("Error retrieving clients", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return clients;
    }
    
    /**
     * Gets a client by ID.
     * 
     * @param clientId The client ID
     * @return The Client object, or null if not found
     */
    public Client getClientById(int clientId) {
        logger.info("Getting client with ID: {}", clientId);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Client client = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT c.ClientId, c.ClientsName, c.Email, c.DateofSubscription, " +
                          "IFNULL(SUM(r.PointsEarned) - SUM(r.PointsUsed), 0) AS RewardPoints " +
                          "FROM Clients c " +
                          "LEFT JOIN Rewards r ON c.ClientId = r.ClientId " +
                          "WHERE c.ClientId = ? " +
                          "GROUP BY c.ClientId, c.ClientsName, c.Email, c.DateofSubscription";
            stmt = conn.prepareCall(query);
            stmt.setInt(1, clientId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                client = new Client();
                client.setClientId(rs.getInt("ClientId"));
                client.setClientName(rs.getString("ClientsName"));
                client.setEmail(rs.getString("Email"));
                client.setDateOfSubscription(rs.getObject("DateofSubscription", LocalDate.class));
                client.setRewardPoints(rs.getInt("RewardPoints"));
                logger.debug("Found client: {}", client);
            } else {
                logger.debug("No client found with ID: {}", clientId);
            }
        } catch (SQLException e) {
            logger.error("Error getting client by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return client;
    }
    
    /**
     * Gets a client's reward points.
     * 
     * @param clientId The client ID
     * @return The reward points, or -1 if client not found
     */
    public int getClientRewardPoints(int clientId) {
        logger.info("Getting reward points for client ID: {}", clientId);
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        int rewardPoints = -1;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT AvailablePoints FROM ClientRewardPoints WHERE ClientId = ?";
            stmt = conn.prepareCall(query);
            stmt.setInt(1, clientId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                rewardPoints = rs.getInt("AvailablePoints");
                logger.debug("Client has {} reward points", rewardPoints);
            } else {
                logger.debug("No reward points found for client ID: {}", clientId);
            }
        } catch (SQLException e) {
            logger.error("Error getting client reward points", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return rewardPoints;
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