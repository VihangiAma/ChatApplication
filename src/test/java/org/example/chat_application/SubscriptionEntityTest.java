package org.example.chat_application;

import org.example.chat_application.model.Subscription;
import org.example.chat_application.model.User;
import org.example.chat_application.model.Chat;
import org.example.chat_application.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.util.Date;

public class SubscriptionEntityTest {
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
                user.setRole(User.Role.user);
                
                // Create a test chat
                Chat chat = new Chat();
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
                
                // Query for subscriptions
                Query<Subscription> query = session.createQuery("FROM Subscription", Subscription.class);
                java.util.List<Subscription> subscriptions = query.list();
                
                System.out.println("Found " + subscriptions.size() + " subscriptions");
                
                // Print details of each subscription
                for (Subscription sub : subscriptions) {
                    System.out.println("Subscription ID: " + sub.getSubscriptionId());
                    System.out.println("User ID: " + sub.getUser().getUser_id());
                    System.out.println("Chat ID: " + sub.getChat().getChatId());
                    System.out.println("-------------------");
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