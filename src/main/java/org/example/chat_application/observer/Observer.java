package org.example.chat_application.observer;

/**
 * Observer interface for the Observer pattern.
 * Classes implementing this interface can receive updates from subjects they are observing.
 */
public interface Observer {
    /**
     * This method is called when the subject being observed is updated.
     * 
     * @param message The message or update from the subject
     */
    void update(String message);

    /**
     * Gets the name of the observer.
     * 
     * @return The name of the observer
     */
    String getName();
}
