package org.example.chat_application.rmi.server;

import org.example.chat_application.dao.impl.ChatDAOImpl;
import org.example.chat_application.dao.impl.SubscriptionDAOImpl;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.Subscription;
import org.example.chat_application.model.User;
import org.example.chat_application.observer.ChatManager;
import org.example.chat_application.observer.ChatRoom;
import org.example.chat_application.observer.ChatUser;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientInterface;
import org.example.chat_application.util.HibernateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final Map<String, ClientInterface> clients = new ConcurrentHashMap<>();
    private final Map<String, User> clientUsers = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String>> chatSubscriptions = new ConcurrentHashMap<>();
    private final Map<Integer, StringBuilder> chatLogs = new ConcurrentHashMap<>();
    private final Map<Integer, String> activeChats = new ConcurrentHashMap<>(); // chatId -> adminName
    private final ChatDAOImpl chatDAO;
    private final SubscriptionDAOImpl subscriptionDAO;
    private final ChatManager chatManager;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ChatServiceImpl() throws RemoteException {
        chatDAO = new ChatDAOImpl(HibernateUtil.getSessionFactory());
        subscriptionDAO = new SubscriptionDAOImpl(HibernateUtil.getSessionFactory());
        chatManager = ChatManager.getInstance();

        // Initialize subscription data from database
        initializeSubscriptionsFromDatabase();
    }

    /**
     * Initializes the in-memory subscription map from the database
     */
    private void initializeSubscriptionsFromDatabase() {
        try {
            // Get all chats
            List<Chat> chats = chatDAO.getAllChats();

            for (Chat chat : chats) {
                int chatId = chat.getChatId();

                // Get subscriptions for this chat
                List<Subscription> subscriptions = subscriptionDAO.getSubscriptionsByChatId(chatId);

                if (subscriptions != null && !subscriptions.isEmpty()) {
                    // Create a set for this chat's subscribers
                    Set<String> subscribers = new HashSet<>();

                    // Add each subscriber to the set
                    for (Subscription subscription : subscriptions) {
                        User user = subscription.getUser();
                        if (user != null) {
                            subscribers.add(user.getUsername());
                        }
                    }

                    // Add to the in-memory map if there are subscribers
                    if (!subscribers.isEmpty()) {
                        chatSubscriptions.put(chatId, subscribers);
                    }
                }
            }

            System.out.println("Initialized subscription data from database. Total chats with subscriptions: " + chatSubscriptions.size());
        } catch (Exception e) {
            System.err.println("Error initializing subscriptions from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void registerClient(ClientInterface client, String name, User user) throws RemoteException {
        clients.put(name, client);
        clientUsers.put(name, user);

        // Create a ChatUser in the ChatManager
        chatManager.getChatUser(user, client);

        // Notify about active chats if user is subscribed
        for (Map.Entry<Integer, String> entry : activeChats.entrySet()) {
            int chatId = entry.getKey();
            String adminName = entry.getValue();

            if (chatSubscriptions.containsKey(chatId) && 
                chatSubscriptions.get(chatId).contains(name)) {
                client.notifyAdminStartedChat(chatId, adminName);
            }
        }
    }

    @Override
    public void broadcastMessage(String message, String senderName) throws RemoteException {
        // Check if this is a "Bye" message to leave the chat
        if (message.trim().equalsIgnoreCase("Bye")) {
            for (Map.Entry<Integer, Set<String>> entry : chatSubscriptions.entrySet()) {
                int chatId = entry.getKey();
                Set<String> subscribers = entry.getValue();

                if (subscribers.contains(senderName)) {
                    unsubscribeFromChat(chatId, senderName);
                    return;
                }
            }
            return;
        }

        // Find which chat this user is in and send message using Observer pattern
        boolean messageSent = false;
        User sender = clientUsers.get(senderName);
        String nickname = sender != null ? sender.getNickname() : senderName;

        for (Map.Entry<Integer, Set<String>> entry : chatSubscriptions.entrySet()) {
            int chatId = entry.getKey();
            Set<String> subscribers = entry.getValue();

            if (subscribers.contains(senderName) && activeChats.containsKey(chatId)) {
                // Use ChatManager to send message (only if user is subscribed)
                ChatRoom chatRoom = chatManager.getChatRoom(chatId);

                if (chatRoom != null && sender != null && chatManager.isUserSubscribed(senderName, chatId)) {
                    // Send message using Observer pattern
                    messageSent = chatManager.sendMessage(senderName, chatId, message);

                    // Log the message if sent successfully
                    if (messageSent && chatLogs.containsKey(chatId)) {
                        chatLogs.get(chatId).append(nickname)
                                .append(": ")
                                .append(message)
                                .append("\n");
                    }

                    break;
                }

                // Fallback to old method if Observer pattern fails
                if (!messageSent) {
                    // Only send to subscribers of this chat
                    for (String subscriber : subscribers) {
                        try {
                            if (!subscriber.equals(senderName)) {
                                clients.get(subscriber).receiveMessage(nickname + ": " + message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Log the message
                    if (chatLogs.containsKey(chatId)) {
                        chatLogs.get(chatId).append(nickname)
                                .append(": ")
                                .append(message)
                                .append("\n");
                    }
                }

                break;
            }
        }
    }

    @Override
    public synchronized void removeClient(String name) throws RemoteException {
        clients.remove(name);
        clientUsers.remove(name);

        // Remove from all chat subscriptions
        for (Map.Entry<Integer, Set<String>> entry : chatSubscriptions.entrySet()) {
            int chatId = entry.getKey();
            if (entry.getValue().contains(name)) {
                unsubscribeFromChat(chatId, name);
            }
        }

        System.out.println("Client '" + name + "' removed.");
    }

    @Override
    public void startChat(int chatId, String adminName) throws RemoteException {
        // Mark chat as active
        activeChats.put(chatId, adminName);

        // Initialize chat log
        String startTime = timeFormat.format(new Date());
        StringBuilder chatLog = new StringBuilder();
        chatLog.append("Chat started at: ").append(startTime).append("\n");
        chatLogs.put(chatId, chatLog);

        // Initialize subscription set if not exists
        if (!chatSubscriptions.containsKey(chatId)) {
            chatSubscriptions.put(chatId, new HashSet<>());
        }

        // Notify subscribed users
        notifySubscribedUsers(chatId, "Chat started by admin: " + adminName);

        // Update chat in database with start time
        try {
            Chat chat = chatDAO.getChatById(chatId);
            if (chat != null) {
                chat.setStartedAt(new Timestamp(System.currentTimeMillis()));
                chatDAO.updateChat(chat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribeToChat(int chatId, String userName) throws RemoteException {
        // Get or create chat room and user
        Chat chat = chatDAO.getChatById(chatId);
        String chatName = "Chat " + chatId;
        if (chat != null) {
            chatName = "Chat " + chat.getChatId();
        }

        // Always ensure the chat subscription map has an entry for this chat
        if (!chatSubscriptions.containsKey(chatId)) {
            chatSubscriptions.put(chatId, new HashSet<>());
        }

        // Get the subscribers set for this chat
        Set<String> subscribers = chatSubscriptions.get(chatId);

        // Always add the username to the subscribers set if not already there
        if (!subscribers.contains(userName)) {
            subscribers.add(userName);
            System.out.println("Added user " + userName + " to chat " + chatId + " subscribers list");
        }

        // Try to get the user and client objects if they're registered
        User user = clientUsers.get(userName);
        ClientInterface client = clients.get(userName);

        // If the user is registered with the RMI service, perform additional actions
        if (user != null && client != null) {
            // Subscribe user to chat using Observer pattern
            ChatRoom chatRoom = chatManager.getChatRoom(chatId, chatName);
            ChatUser chatUser = chatManager.getChatUser(user, client);
            chatUser.subscribe(chatRoom);

            // Save subscription to database
            try {
                // Check if subscription already exists
                Subscription existingSubscription = subscriptionDAO.getSubscriptionByUserAndChat(user.getUser_id(), chatId);
                if (existingSubscription == null) {
                    // Create new subscription
                    Subscription subscription = new Subscription();
                    subscription.setUser(user);
                    subscription.setChat(chat);
                    subscriptionDAO.saveSubscription(subscription);
                    System.out.println("Saved subscription to database for user " + userName + " to chat " + chatId);
                }
            } catch (Exception e) {
                System.err.println("Error saving subscription to database: " + e.getMessage());
                e.printStackTrace();
            }

            // Notify other subscribers that this user joined
            String joinTime = timeFormat.format(new Date());
            String nickname = user.getNickname() != null ? user.getNickname() : userName;

            for (String subscriber : subscribers) {
                if (!subscriber.equals(userName)) {
                    try {
                        ClientInterface subscriberClient = clients.get(subscriber);
                        if (subscriberClient != null) {
                            subscriberClient.notifyUserJoined(nickname, joinTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Log the join event
            if (chatLogs.containsKey(chatId)) {
                chatLogs.get(chatId).append("\"")
                        .append(nickname)
                        .append("\" has joined: ")
                        .append(joinTime)
                        .append("\n");
            }

            // Notify the user that they joined the chat
            if (clients.containsKey(userName)) {
                clients.get(userName).notifyChatStarted(chatId, joinTime);
            }
        }
    }

    @Override
    public void unsubscribeFromChat(int chatId, String userName) throws RemoteException {
        // Unsubscribe user from chat using Observer pattern
        ChatRoom chatRoom = chatManager.getChatRoom(chatId);
        ChatUser chatUser = chatManager.getChatUser(userName);

        if (chatRoom != null && chatUser != null) {
            chatUser.unsubscribe(chatRoom);
        }

        // Maintain backward compatibility with existing code
        if (chatSubscriptions.containsKey(chatId)) {
            Set<String> subscribers = chatSubscriptions.get(chatId);
            if (subscribers.contains(userName)) {
                subscribers.remove(userName);

                // Delete subscription from database
                try {
                    User user = clientUsers.get(userName);
                    if (user != null) {
                        boolean deleted = subscriptionDAO.deleteSubscriptionByUserAndChat(user.getUser_id(), chatId);
                        if (deleted) {
                            System.out.println("Deleted subscription from database for user " + userName + " from chat " + chatId);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting subscription from database: " + e.getMessage());
                    e.printStackTrace();
                }

                // Notify other subscribers that this user left
                String leaveTime = timeFormat.format(new Date());
                User user = clientUsers.get(userName);
                String nickname = user != null ? user.getNickname() : userName;

                for (String subscriber : subscribers) {
                    try {
                        ClientInterface subscriberClient = clients.get(subscriber);
                        if (subscriberClient != null) {
                            subscriberClient.notifyUserLeft(nickname, leaveTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Log the leave event
                if (chatLogs.containsKey(chatId)) {
                    chatLogs.get(chatId).append("\"")
                            .append(nickname)
                            .append("\" left: ")
                            .append(leaveTime)
                            .append("\n");
                }

                // If this was the last user, end the chat
                if (subscribers.isEmpty()) {
                    endChat(chatId);
                }
            }
        }
    }

    @Override
    public List<String> getSubscribedUsers(int chatId) throws RemoteException {
        // First check in-memory cache
        if (chatSubscriptions.containsKey(chatId)) {
            return new ArrayList<>(chatSubscriptions.get(chatId));
        }

        // If not in cache, try to load from database
        try {
            List<Subscription> subscriptions = subscriptionDAO.getSubscriptionsByChatId(chatId);
            if (subscriptions != null && !subscriptions.isEmpty()) {
                // Create a new set for this chat if it doesn't exist
                if (!chatSubscriptions.containsKey(chatId)) {
                    chatSubscriptions.put(chatId, new HashSet<>());
                }

                Set<String> subscribers = chatSubscriptions.get(chatId);
                List<String> usernames = new ArrayList<>();

                for (Subscription subscription : subscriptions) {
                    User user = subscription.getUser();
                    if (user != null) {
                        String username = user.getUsername();
                        subscribers.add(username); // Update in-memory cache
                        usernames.add(username);
                    }
                }

                return usernames;
            }
        } catch (Exception e) {
            System.err.println("Error loading subscriptions from database: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public List<String> getActiveUsers(int chatId) throws RemoteException {
        List<String> activeUsers = new ArrayList<>();

        if (chatSubscriptions.containsKey(chatId)) {
            Set<String> subscribers = chatSubscriptions.get(chatId);

            // Filter subscribers to only include those who are currently connected
            for (String username : subscribers) {
                if (clients.containsKey(username)) {
                    activeUsers.add(username);
                }
            }
        }

        return activeUsers;
    }

    @Override
    public void notifySubscribedUsers(int chatId, String message) throws RemoteException {
        if (chatSubscriptions.containsKey(chatId)) {
            Set<String> subscribers = chatSubscriptions.get(chatId);
            for (String subscriber : subscribers) {
                try {
                    if (clients.containsKey(subscriber)) {
                        clients.get(subscriber).receiveMessage("SYSTEM: " + message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void endChat(int chatId) throws RemoteException {
        if (activeChats.containsKey(chatId)) {
            // Get end time
            String endTime = timeFormat.format(new Date());

            // Log the end event
            if (chatLogs.containsKey(chatId)) {
                chatLogs.get(chatId).append("Chat stopped at: ")
                        .append(endTime)
                        .append("\n");
            }

            // Notify all subscribers that the chat has ended
            if (chatSubscriptions.containsKey(chatId)) {
                Set<String> subscribers = new HashSet<>(chatSubscriptions.get(chatId));
                for (String subscriber : subscribers) {
                    try {
                        if (clients.containsKey(subscriber)) {
                            clients.get(subscriber).notifyChatEnded(endTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                chatSubscriptions.get(chatId).clear();
            }

            // Save chat log to file
            saveChatLogToFile(chatId);

            // Update chat in database with end time
            try {
                Chat chat = chatDAO.getChatById(chatId);
                if (chat != null) {
                    chat.setEndedAt(new Timestamp(System.currentTimeMillis()));
                    chatDAO.updateChat(chat);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Remove from active chats
            activeChats.remove(chatId);
            chatLogs.remove(chatId);
        }
    }

    private void saveChatLogToFile(int chatId) {
        if (chatLogs.containsKey(chatId)) {
            try {
                // Create directory if it doesn't exist
                File dir = new File("chat_logs");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Create file
                String fileName = "chat_logs/chat_" + chatId + "_" + System.currentTimeMillis() + ".txt";
                File file = new File(fileName);

                // Write to file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(chatLogs.get(chatId).toString());
                }

                // Update chat in database with file path
                try {
                    Chat chat = chatDAO.getChatById(chatId);
                    if (chat != null) {
                        chat.setFilePath(fileName);
                        chatDAO.updateChat(chat);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Chat log saved to: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void notifyAllUsersAboutNewChat(int chatId, String adminName) throws RemoteException {
        // Notify all connected clients about the new chat and subscribe them to the chat
        for (Map.Entry<String, ClientInterface> entry : clients.entrySet()) {
            try {
                // Notify the client about the new chat
                entry.getValue().notifyAdminStartedChat(chatId, adminName);

                // Automatically subscribe the user to the chat
                subscribeToChat(chatId, entry.getKey());

                System.out.println("User " + entry.getKey() + " automatically subscribed to chat " + chatId);
            } catch (Exception e) {
                System.err.println("Error notifying/subscribing client " + entry.getKey() + " about new chat: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Notified and subscribed all users to new chat " + chatId + " created by admin " + adminName);
    }
}
