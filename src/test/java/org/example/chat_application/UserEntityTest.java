package org.example.chat_application;

import org.example.chat_application.model.User;
import org.example.chat_application.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class UserEntityTest {
    public static void main(String[] args) {
        // Get the SessionFactory
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        
        // Open a new Session
        try (Session session = sessionFactory.openSession()) {
            System.out.println("Session opened successfully");
            
            // Try to execute a simple HQL query to get all users
            try {
                Query<User> query = session.createQuery("FROM User", User.class);
                System.out.println("Query created successfully");
                
                // Execute the query
                java.util.List<User> users = query.list();
                System.out.println("Query executed successfully");
                
                // Print the number of users found
                System.out.println("Found " + users.size() + " users");
                
                // Print details of each user
                for (User user : users) {
                    System.out.println("User ID: " + user.getUser_id());
                    System.out.println("Username: " + user.getUsername());
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Role: " + user.getRole());
                    System.out.println("Nickname: " + user.getNickname());
                    System.out.println("-------------------");
                }
            } catch (Exception e) {
                System.err.println("Error executing query: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error opening session: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Shutdown Hibernate
            HibernateUtil.shutdown();
        }
    }
}