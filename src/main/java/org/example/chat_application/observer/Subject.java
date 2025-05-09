package org.example.chat_application.observer;

import java.util.List;

/**
 * Subject interface for the Observer pattern.
 * Classes implementing this interface can be observed by Observer objects.
 */
public interface Subject {
    /**
     * Registers an observer to receive updates from this subject.
     * 
     * @param observer The observer to register
     */
    void registerObserver(Observer observer);

    /**
     * Removes an observer so it no longer receives updates from this subject.
     * 
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notifies all registered observers with a message.
     * 
     * @param message The message to send to all observers
     */
    void notifyObservers(String message);

    /**
     * Gets a list of all registered observers.
     * 
     * @return List of registered observers
     */
    List<Observer> getObservers();
}
