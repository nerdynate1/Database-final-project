package edu.csc3810.flightservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("Database Connection Test");
        System.out.println("========================");
        
        // Try to load properties file
        System.out.println("Loading properties file...");
        Properties props = new Properties();
        try (InputStream input = DatabaseConnectionTest.class.getResourceAsStream("/database.properties")) {
            if (input == null) {
                System.out.println("ERROR: Cannot find database.properties");
                return;
            }
            props.load(input);
            System.out.println("Properties file loaded successfully");
            
            // Display properties
            String dbDriver = props.getProperty("db.driver");
            String dbUrl = props.getProperty("db.url");
            String dbUsername = props.getProperty("db.username");
            String dbPassword = props.getProperty("db.password");
            
            System.out.println("db.driver: " + dbDriver);
            System.out.println("db.url: " + dbUrl);
            System.out.println("db.username: " + dbUsername);
            System.out.println("db.password length: " + (dbPassword != null ? dbPassword.length() : 0));
            
            // Load driver
            System.out.println("Loading driver...");
            Class.forName(dbDriver);
            System.out.println("Driver loaded successfully");
            
            // Try connection
            System.out.println("Attempting connection...");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                System.out.println("CONNECTION SUCCESSFUL!");
                System.out.println("Database product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
            }
            
        } catch (IOException e) {
            System.out.println("ERROR loading properties: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR loading driver: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("ERROR connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 