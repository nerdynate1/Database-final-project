package edu.csc3810.flightservice.business;

/**
 * Exception thrown by service layer classes.
 */
public class ServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new service exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new service exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new service exception with the specified cause.
     * 
     * @param cause the cause
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
} 