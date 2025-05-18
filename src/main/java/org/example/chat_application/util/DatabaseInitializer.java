package org.example.chat_application.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to initialize the database by executing SQL scripts
 */
public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root0000";
    private static final String DB_NAME = "chat_app";
    private static final String SQL_SCRIPT_PATH = "db/chatapplication.sql";

    /**
     * Initializes the database by executing the SQL script
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First, try to connect to MySQL server (without specifying a database)
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("Connected to MySQL server");
                
                // Execute the SQL script
                executeSqlScript(conn);
                
                System.out.println("Database initialized successfully");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading SQL script: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executes the SQL script
     */
    private static void executeSqlScript(Connection conn) throws IOException, SQLException {
        System.out.println("Executing SQL script: " + SQL_SCRIPT_PATH);
        
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(SQL_SCRIPT_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                
                sb.append(line);
                
                // Execute statement when semicolon is found
                if (line.trim().endsWith(";")) {
                    String sql = sb.toString();
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql);
                        System.out.println("Executed SQL: " + sql);
                    }
                    sb.setLength(0);
                }
            }
        }
    }
}