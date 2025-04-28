package edu.csc3810.flightservice.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.csc3810.flightservice.dal.ClientDAO;
import edu.csc3810.flightservice.model.Client;

/**
 * Business logic for client operations.
 */
public class ClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    
    private final ClientDAO clientDAO;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param clientDAO The client data access object
     */
    public ClientService(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }
    
    /**
     * Gets all clients in the system.
     * 
     * @return List of Client objects
     * @throws ServiceException If a database error occurs
     */
    public List<Client> getAllClients() throws ServiceException {
        logger.info("Getting all clients");
        
        List<Client> clients = clientDAO.getAllClients();
        logger.debug("Retrieved {} clients", clients.size());
        
        return clients;
    }
    
    /**
     * Gets details of a specific client.
     * 
     * @param clientId The client ID
     * @return The Client object
     * @throws ServiceException If the client is not found or a database error occurs
     */
    public Client getClientDetails(int clientId) throws ServiceException {
        logger.info("Getting details for client ID: {}", clientId);
        
        if (clientId <= 0) {
            throw new ServiceException("Invalid client ID");
        }
        
        Client client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ServiceException("Client not found with ID: " + clientId);
        }
        
        return client;
    }
    
    /**
     * Gets a client's reward points.
     * 
     * @param clientId The client ID
     * @return The reward points
     * @throws ServiceException If the client is not found or a database error occurs
     */
    public int getClientRewardPoints(int clientId) throws ServiceException {
        logger.info("Getting reward points for client ID: {}", clientId);
        
        if (clientId <= 0) {
            throw new ServiceException("Invalid client ID");
        }
        
        // Verify client exists
        Client client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ServiceException("Client not found with ID: " + clientId);
        }
        
        int rewardPoints = clientDAO.getClientRewardPoints(clientId);
        if (rewardPoints < 0) {
            throw new ServiceException("Failed to retrieve reward points");
        }
        
        logger.debug("Client has {} reward points", rewardPoints);
        return rewardPoints;
    }
} 