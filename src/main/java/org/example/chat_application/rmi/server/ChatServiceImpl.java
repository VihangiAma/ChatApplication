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
    public synchronized void registerClient(ClientInterface client, String name, User user) throws RemoteException {
        System.out.println("Registering client: " + name);

        // Store client and user information
        clients.put(name, client);
        clientUsers.put(name, user);

        // Set user as online
        user.setOnline(true);

        // Broadcast online status to all clients
        broadcastUserOnlineStatus(name, true);

        // Create a ChatUser in the ChatManager
        ChatUser chatUser = chatManager.getChatUser(user, client);

        // Check all chats that this user is subscribed to
        for (Map.Entry<Integer, Set<String>> entry : chatSubscriptions.entrySet()) {
            int chatId = entry.getKey();
            Set<String> subscribers = entry.getValue();

            if (subscribers.contains(name)) {
                System.out.println("User " + name + " is subscribed to chat " + chatId + ", ensuring proper subscription");

                // Get the chat room
                Chat chat = chatDAO.getChatById(chatId);
                String chatName = "Chat " + chatId;
                if (chat != null) {
                    chatName = chat.getName() != null && !chat.getName().isEmpty() ? 
                              chat.getName() : "Chat " + chat.getChatId();
                }

                // Get or create the chat room in the ChatManager
                ChatRoom chatRoom = chatManager.getChatRoom(chatId, chatName);

                // Subscribe the user to the chat room using Observer pattern
                chatUser.subscribe(chatRoom);

                // Notify the user about this chat if it's active
                if (activeChats.containsKey(chatId)) {
                    String adminName = activeChats.get(chatId);
                    client.notifyAdminStartedChat(chatId, adminName);
                    System.out.println("Notified user " + name + " about active chat " + chatId);
                }
            }
        }

        System.out.println("Client registration complete for: " + name);
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
        // Set user as offline before removing
        User user = clientUsers.get(name);
        if (user != null) {
            user.setOnline(false);

            // Broadcast offline status to all clients
            broadcastUserOnlineStatus(name, false);
        }

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
        activeChats.put(chatId, adminName);

        String startTime = timeFormat.format(new Date());
        StringBuilder chatLog = new StringBuilder();
        chatLog.append("Chat started at: ").append(startTime).append("\n");
        chatLogs.put(chatId, chatLog);

        if (!chatSubscriptions.containsKey(chatId)) {
            chatSubscriptions.put(chatId, new HashSet<>());
        }

        // ✅ New: Notify online & subscribed users through dashboard
        for (String username : chatSubscriptions.get(chatId)) {
            if (clients.containsKey(username)) {
                try {
                    clients.get(username).receiveDashboardNotification(
                            "Admin " + adminName + " started a new chat (ID: " + chatId + ")");
                } catch (Exception e) {
                    System.err.println("Error sending notification to " + username + ": " + e.getMessage());
                }
            }
        }


        // Existing logic
        notifySubscribedUsers(chatId, "Chat started by admin: " + adminName);

        // Update DB
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
    public synchronized void subscribeToChat(int chatId, String userName) throws RemoteException {
        // Get or create chat room and user
        Chat chat = chatDAO.getChatById(chatId);
        String chatName = chat != null && chat.getName() != null && !chat.getName().isEmpty()
                ? chat.getName() : "Chat " + chatId;

        chatSubscriptions.putIfAbsent(chatId, new HashSet<>());

        Set<String> subscribers = chatSubscriptions.get(chatId);

        if (subscribers.contains(userName)) {
            System.out.println("User " + userName + " is already subscribed to chat " + chatId);
            return;
        }

        subscribers.add(userName);
        System.out.println("Added user " + userName + " to chat " + chatId + " subscribers list");

        ChatRoom chatRoom = chatManager.getChatRoom(chatId, chatName);
        User user = clientUsers.get(userName);
        ClientInterface client = clients.get(userName);

        if (user != null && client != null) {
            ChatUser chatUser = chatManager.getChatUser(user, client);
            chatUser.subscribe(chatRoom);

            try {
                Subscription existing = subscriptionDAO.getSubscriptionByUserAndChat(user.getUser_id(), chatId);
                if (existing == null && chat != null) {
                    Subscription newSub = new Subscription();
                    newSub.setUser(user);
                    newSub.setChat(chat);
                    subscriptionDAO.saveSubscription(newSub);
                    System.out.println("Saved subscription for " + userName);
                }
            } catch (Exception e) {
                System.err.println("Error saving subscription: " + e.getMessage());
                e.printStackTrace();
            }

            String joinTime = timeFormat.format(new Date());
            String nickname = user.getNickname() != null ? user.getNickname() : userName;

            for (String subscriber : subscribers) {
                try {
                    ClientInterface subscriberClient = clients.get(subscriber);
                    if (subscriberClient != null) {
                        subscriberClient.notifyUserJoined(nickname, joinTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 👇 Send join event as SYSTEM message (chat bubble) to the joining user as well
            if (client != null) {
                client.receiveMessage("SYSTEM: \"" + nickname + "\" has joined: " + joinTime);
            }

            if (chatLogs.containsKey(chatId)) {
                chatLogs.get(chatId).append("\"")
                        .append(nickname)
                        .append("\" has joined: ")
                        .append(joinTime)
                        .append("\n");
            }

            try {
                String start = chat != null && chat.getStartedAt() != null
                        ? timeFormat.format(chat.getStartedAt()) : joinTime;
                client.notifyChatStarted(chatId, start);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("User " + userName + " is not yet registered with RMI.");
        }
    }


    @Override
    public synchronized void unsubscribeFromChat(int chatId, String userName) throws RemoteException {
        // Check if the chat exists in our subscriptions map
        if (!chatSubscriptions.containsKey(chatId)) {
            System.out.println("Chat " + chatId + " not found in subscriptions map");
            return;
        }

        // Check if the user is subscribed to this chat
        Set<String> subscribers = chatSubscriptions.get(chatId);
        if (!subscribers.contains(userName)) {
            System.out.println("User " + userName + " is not subscribed to chat " + chatId);
            return;
        }

        // Unsubscribe user from chat using Observer pattern if possible
        ChatRoom chatRoom = chatManager.getChatRoom(chatId);
        if (chatRoom != null) {
            ChatUser chatUser = chatManager.getChatUser(userName);
            if (chatUser != null) {
                chatUser.unsubscribe(chatRoom);
                System.out.println("Unsubscribed user " + userName + " from chat " + chatId + " using Observer pattern");
            }
        }

        // Remove from subscriptions map
        subscribers.remove(userName);
        System.out.println("Removed user " + userName + " from chat " + chatId + " subscribers list");

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
            String endTime = timeFormat.format(new Date());

            if (chatLogs.containsKey(chatId)) {
                chatLogs.get(chatId).append("Chat stopped at: ").append(endTime).append("\n");
            }

            if (chatSubscriptions.containsKey(chatId)) {
                Set<String> subscribers = new HashSet<>(chatSubscriptions.get(chatId));

                // 👇 Send system message to all users (display as chat bubble)
                notifySubscribedUsers(chatId, "Chat stopped at: " + endTime);

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

            saveChatLogToFile(chatId);

            try {
                Chat chat = chatDAO.getChatById(chatId);
                if (chat != null) {
                    chat.setEndedAt(new Timestamp(System.currentTimeMillis()));
                    chatDAO.updateChat(chat);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    @Override
    public boolean isUserOnline(String username) throws RemoteException {
        User user = clientUsers.get(username);
        return user != null && user.isOnline();
    }

    @Override
    public List<User> getOnlineUsers() throws RemoteException {
        List<User> onlineUsers = new ArrayList<>();
        for (User user : clientUsers.values()) {
            if (user.isOnline()) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    /**
     * Broadcasts a user's online status to all connected clients
     * @param username The username of the user whose status changed
     * @param isOnline The new online status
     */
    private void broadcastUserOnlineStatus(String username, boolean isOnline) {
        for (Map.Entry<String, ClientInterface> entry : clients.entrySet()) {
            try {
                // Don't notify the user about their own status change
                if (!entry.getKey().equals(username)) {
                    entry.getValue().updateUserOnlineStatus(username, isOnline);
                }
            } catch (Exception e) {
                System.err.println("Error notifying client " + entry.getKey() + " about user status change: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Broadcasted " + username + " status change to " + (clients.size() - 1) + " clients. Status: " + (isOnline ? "Online" : "Offline"));
    }
}
