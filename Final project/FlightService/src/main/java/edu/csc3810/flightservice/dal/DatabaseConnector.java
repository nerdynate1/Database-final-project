package edu.csc3810.flightservice.dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles database connections for the application.
 */
public class DatabaseConnector {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);
    private static final String PROPERTIES_FILE = "/database.properties";
    
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;
    
    static {
        try {
            loadDatabaseProperties();
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load database driver", e);
            throw new RuntimeException("Failed to load database driver", e);
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
            throw new RuntimeException("Failed to load database properties", e);
        }
    }
    
    /**
     * Loads database connection properties from the properties file.
     * 
     * @throws IOException If the properties file cannot be read
     */
    private static void loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnector.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Cannot find " + PROPERTIES_FILE);
            }
            props.load(input);
            
            dbDriver = props.getProperty("db.driver");
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            
            logger.info("Database driver: {}", dbDriver);
            logger.info("Database URL: {}", dbUrl);
            logger.info("Database username: {}", dbUsername);
            logger.info("Database password length: {}", dbPassword != null ? dbPassword.length() : 0);
            
            logger.info("Database properties loaded successfully");
        }
    }
    
    /**
     * Gets a connection to the database.
     * 
     * @return A new database connection
     * @throws SQLException If connection fails
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Getting database connection");
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
    
    /**
     * Closes a database connection safely.
     * 
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
} 