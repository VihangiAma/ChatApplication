package org.example.chat_application.dao;

import org.example.chat_application.model.Subscription;
import java.util.List;

/**
 * Data Access Object interface for Subscription entity
 */
public interface SubscriptionDAO {
    
    /**
     * Saves a subscription to the database
     * 
     * @param subscription The subscription to save
     */
    void saveSubscription(Subscription subscription);
    
    /**
     * Gets a subscription by its ID
     * 
     * @param id The ID of the subscription
     * @return The subscription, or null if not found
     */
    Subscription getSubscriptionById(int id);
    
    /**
     * Gets all subscriptions for a user
     * 
     * @param userId The ID of the user
     * @return A list of subscriptions
     */
    List<Subscription> getSubscriptionsByUserId(int userId);
    
    /**
     * Gets all subscriptions for a chat
     * 
     * @param chatId The ID of the chat
     * @return A list of subscriptions
     */
    List<Subscription> getSubscriptionsByChatId(int chatId);
    
    /**
     * Gets a subscription by user ID and chat ID
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return The subscription, or null if not found
     */
    Subscription getSubscriptionByUserAndChat(int userId, int chatId);
    
    /**
     * Deletes a subscription
     * 
     * @param subscription The subscription to delete
     */
    void deleteSubscription(Subscription subscription);
    
    /**
     * Deletes a subscription by user ID and chat ID
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return true if a subscription was deleted, false otherwise
     */
    boolean deleteSubscriptionByUserAndChat(int userId, int chatId);
}