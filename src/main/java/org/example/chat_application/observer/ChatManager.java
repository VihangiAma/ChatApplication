package org.example.chat_application.observer;

import org.example.chat_application.model.User;
import org.example.chat_application.rmi.client.ClientInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for chat rooms and users.
 * This class is responsible for creating and managing chat rooms and users,
 * and provides methods for subscribing users to chat rooms and sending messages.
 */
public class ChatManager {
    private static ChatManager instance;
    private final Map<Integer, ChatRoom> chatRooms;
    private final Map<String, ChatUser> chatUsers;

    /**
     * Private constructor for singleton pattern.
     */
    private ChatManager() {
        chatRooms = new ConcurrentHashMap<>();
        chatUsers = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton instance of the chat manager.
     *
     * @return The chat manager instance
     */
    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    /**
     * Creates a new chat room or returns an existing one.
     *
     * @param chatId The unique identifier for the chat room
     * @param chatName The name of the chat room
     * @return The chat room
     */
    public ChatRoom getChatRoom(int chatId, String chatName) {
        if (!chatRooms.containsKey(chatId)) {
            chatRooms.put(chatId, new ChatRoom(chatId, chatName));
        }
        return chatRooms.get(chatId);
    }

    /**
     * Gets a chat room by its ID.
     *
     * @param chatId The unique identifier for the chat room
     * @return The chat room, or null if it doesn't exist
     */
    public ChatRoom getChatRoom(int chatId) {
        return chatRooms.get(chatId);
    }

    /**
     * Removes a chat room.
     *
     * @param chatId The unique identifier for the chat room
     */
    public void removeChatRoom(int chatId) {
        chatRooms.remove(chatId);
    }

    /**
     * Creates a new chat user or returns an existing one.
     *
     * @param user The user entity
     * @param clientInterface The client interface for sending messages to the user's client
     * @return The chat user
     */
    public ChatUser getChatUser(User user, ClientInterface clientInterface) {
        String username = user.getUsername();
        if (!chatUsers.containsKey(username)) {
            chatUsers.put(username, new ChatUser(user, clientInterface));
        } else {
            // Update the existing ChatUser's clientInterface to ensure it's using the latest one
            ChatUser existingUser = chatUsers.get(username);
            if (existingUser != null && clientInterface != null && 
                !existingUser.getClientInterface().equals(clientInterface)) {
                // Replace with updated user that has the new clientInterface
                chatUsers.put(username, new ChatUser(user, clientInterface));
                System.out.println("Updated clientInterface for user: " + username);
            }
        }
        return chatUsers.get(username);
    }

    /**
     * Gets a chat user by their username.
     *
     * @param username The username of the chat user
     * @return The chat user, or null if they don't exist
     */
    public ChatUser getChatUser(String username) {
        return chatUsers.get(username);
    }

    /**
     * Removes a chat user.
     *
     * @param username The username of the chat user
     */
    public void removeChatUser(String username) {
        ChatUser user = chatUsers.get(username);
        if (user != null) {
            // Unsubscribe the user from all chat rooms
            for (ChatRoom chatRoom : chatRooms.values()) {
                if (chatRoom.isUserSubscribed(username)) {
                    user.unsubscribe(chatRoom);
                }
            }
            chatUsers.remove(username);
        }
    }

    /**
     * Subscribes a user to a chat room.
     *
     * @param username The username of the chat user
     * @param chatId The unique identifier for the chat room
     * @return true if the subscription was successful, false otherwise
     */
    public boolean subscribeUserToChat(String username, int chatId) {
        ChatUser user = chatUsers.get(username);
        ChatRoom chatRoom = chatRooms.get(chatId);

        if (user != null && chatRoom != null) {
            user.subscribe(chatRoom);
            return true;
        }
        return false;
    }

    /**
     * Unsubscribes a user from a chat room.
     *
     * @param username The username of the chat user
     * @param chatId The unique identifier for the chat room
     * @return true if the unsubscription was successful, false otherwise
     */
    public boolean unsubscribeUserFromChat(String username, int chatId) {
        ChatUser user = chatUsers.get(username);
        ChatRoom chatRoom = chatRooms.get(chatId);

        if (user != null && chatRoom != null) {
            user.unsubscribe(chatRoom);
            return true;
        }
        return false;
    }

    /**
     * Sends a message from a user to a chat room.
     *
     * @param username The username of the chat user
     * @param chatId The unique identifier for the chat room
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(String username, int chatId, String message) {
        ChatUser user = chatUsers.get(username);
        ChatRoom chatRoom = chatRooms.get(chatId);

        if (user != null && chatRoom != null) {
            return user.sendMessage(chatRoom, message);
        }
        return false;
    }

    /**
     * Checks if a user is subscribed to a chat room.
     *
     * @param username The username of the chat user
     * @param chatId The unique identifier for the chat room
     * @return true if the user is subscribed, false otherwise
     */
    public boolean isUserSubscribed(String username, int chatId) {
        ChatRoom chatRoom = chatRooms.get(chatId);
        return chatRoom != null && chatRoom.isUserSubscribed(username);
    }
}
