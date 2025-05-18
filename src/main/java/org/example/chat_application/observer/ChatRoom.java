package org.example.chat_application.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Concrete implementation of the Subject interface for chat rooms.
 * This class manages observers (subscribers) for a chat room and notifies them when new messages are sent.
 */
public class ChatRoom implements Subject {
    private final int chatId;
    private final List<Observer> observers;
    private final String chatName;

    /**
     * Constructor for creating a new chat room.
     *
     * @param chatId The unique identifier for this chat room
     * @param chatName The name of this chat room
     */
    public ChatRoom(int chatId, String chatName) {
        this.chatId = chatId;
        this.chatName = chatName;
        // Using CopyOnWriteArrayList for thread safety when iterating over observers
        this.observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Gets the unique identifier for this chat room.
     *
     * @return The chat room ID
     */
    public int getChatId() {
        return chatId;
    }

    /**
     * Gets the name of this chat room.
     *
     * @return The chat room name
     */
    public String getChatName() {
        return chatName;
    }

    @Override
    public void registerObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer " + observer.getName() + " registered to chat room " + chatId);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        System.out.println("Observer " + observer.getName() + " removed from chat room " + chatId);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    @Override
    public List<Observer> getObservers() {
        return new ArrayList<>(observers);
    }

    /**
     * Broadcasts a message from a sender to all observers except the sender.
     *
     * @param message The message to broadcast
     * @param senderName The name of the message sender
     */
    public void broadcastMessage(String message, String senderName) {
        for (Observer observer : observers) {
            if (!observer.getName().equals(senderName)) {
                observer.update(senderName + ": " + message);
            }
        }
    }

    /**
     * Checks if a user is subscribed to this chat room.
     *
     * @param userName The name of the user to check
     * @return true if the user is subscribed, false otherwise
     */
    public boolean isUserSubscribed(String userName) {
        for (Observer observer : observers) {
            if (observer.getName().equals(userName)) {
                return true;
            }
        }
        return false;
    }
}