package org.example.chat_application;

import org.example.chat_application.model.Chat;
import org.example.chat_application.model.Subscription;
import org.example.chat_application.model.User;
import org.example.chat_application.rmi.server.ChatServiceImpl;
import org.example.chat_application.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ChatServiceSubscriptionTest {
    public static void main(String[] args) {
        // Get the SessionFactory
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        
        // Open a new Session
        try (Session session = sessionFactory.openSession()) {
            System.out.println("Session opened successfully");
            
            // Start a transaction
            Transaction tx = session.beginTransaction();
            
            try {
                // Create a test user
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPassword("password");
                user.setNickname("Test User");
                user.setRole(User.Role.user);
                
                // Create a test chat
                Chat chat = new Chat();
                chat.setName("Test Chat");
                chat.setDescription("A test chat for subscription testing");
                chat.setStartedAt(new Timestamp(new Date().getTime()));
                
                // Save the user and chat
                session.save(user);
                session.save(chat);
                
                // Create a subscription
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setChat(chat);
                
                // Save the subscription
                session.save(subscription);
                
                // Commit the transaction
                tx.commit();
                System.out.println("Test data saved successfully");
                
                // Create a ChatServiceImpl instance
                ChatServiceImpl chatService = new ChatServiceImpl();
                
                // Test getSubscribedUsers method
                List<String> subscribedUsers = chatService.getSubscribedUsers(chat.getChatId());
                
                System.out.println("Found " + subscribedUsers.size() + " subscribed users for chat ID " + chat.getChatId());
                
                // Print details of each subscribed user
                for (String username : subscribedUsers) {
                    System.out.println("Subscribed user: " + username);
                }
                
                // Verify that the test user is in the list
                boolean userFound = subscribedUsers.contains(user.getUsername());
                System.out.println("Test user found in subscribed users list: " + userFound);
                
                if (userFound) {
                    System.out.println("TEST PASSED: Subscription data is correctly retrieved from the database");
                } else {
                    System.out.println("TEST FAILED: Subscription data is not correctly retrieved from the database");
                }
                
            } catch (Exception e) {
                // Rollback the transaction if an error occurs
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                System.err.println("Error executing test: " + e.getMessage());
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