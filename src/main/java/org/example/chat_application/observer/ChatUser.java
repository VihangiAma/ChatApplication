package org.example.chat_application.observer;

import org.example.chat_application.model.User;
import org.example.chat_application.rmi.client.ClientInterface;

import java.rmi.RemoteException;

/**
 * Concrete implementation of the Observer interface for chat users.
 * This class represents a user who can subscribe to chat rooms and receive messages from them.
 */
public class ChatUser implements Observer {
    private final User user;
    private final ClientInterface clientInterface;

    /**
     * Constructor for creating a new chat user.
     *
     * @param user The user entity
     * @param clientInterface The client interface for sending messages to the user's client
     */
    public ChatUser(User user, ClientInterface clientInterface) {
        this.user = user;
        this.clientInterface = clientInterface;
    }

    /**
     * Gets the user entity.
     *
     * @return The user entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the client interface.
     *
     * @return The client interface
     */
    public ClientInterface getClientInterface() {
        return clientInterface;
    }

    @Override
    public void update(String message) {
        try {
            // Send the message to the user's client
            clientInterface.receiveMessage(message);
        } catch (RemoteException e) {
            System.err.println("Error sending message to user " + getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    /**
     * Subscribes this user to a chat room.
     *
     * @param chatRoom The chat room to subscribe to
     */
    public void subscribe(ChatRoom chatRoom) {
        chatRoom.registerObserver(this);
    }

    /**
     * Unsubscribes this user from a chat room.
     *
     * @param chatRoom The chat room to unsubscribe from
     */
    public void unsubscribe(ChatRoom chatRoom) {
        chatRoom.removeObserver(this);
    }

    /**
     * Sends a message to a chat room.
     *
     * @param chatRoom The chat room to send the message to
     * @param message The message to send
     * @return true if the message was sent successfully, false if the user is not subscribed to the chat room
     */
    public boolean sendMessage(ChatRoom chatRoom, String message) {
        if (chatRoom.isUserSubscribed(getName())) {
            chatRoom.broadcastMessage(message, getName());
            return true;
        }
        return false;
    }
}