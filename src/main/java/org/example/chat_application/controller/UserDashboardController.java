package org.example.chat_application.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.chat_application.dao.UserDAO;
import org.example.chat_application.dao.impl.ChatDAOImpl;
import org.example.chat_application.dao.impl.UserDAOImpl;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.User;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientInterface;
import org.example.chat_application.util.HibernateUtil;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardController extends UnicastRemoteObject implements ClientInterface {

    @FXML
    private AnchorPane NotificatinPane;

    @FXML
    private AnchorPane ProfilePane;

    @FXML
    private Button change;

    @FXML
    private AnchorPane chats;

    @FXML
    private TextField email; // Assuming email field is shown but not updated.

    @FXML
    private Button logoutBtn;

    @FXML
    private AnchorPane myChatPane;

    @FXML
    private Button myChats;

    @FXML
    private TextField nickname;

    @FXML
    private Button notifi;

    @FXML
    private Button profileSettings;

    @FXML
    private PasswordField pwd;

    @FXML
    private TextField uname;

    @FXML
    private Button update;

    @FXML
    private VBox vBoxMenu;

    @FXML
    private ScrollPane allChatsScrollPane;

    @FXML
    private VBox allChatsVBox;

    @FXML
    private ScrollPane subscribedChatsScrollPane;

    @FXML
    private VBox subscribedChatsVBox;

    // List to store subscribed chat IDs
    private List<Integer> subscribedChatIds = new ArrayList<>();

    // RMI chat service
    private ChatService chatService;

    @FXML
    private Button newChatBtn;

    @FXML
    private ListView<String> notificationListView;

    @FXML
    private Button clearNotificationsBtn;

    @FXML
    private Button refreshNotificationsBtn;

    private File selectedImageFile;
    private int notificationCount = 2;

    @FXML
    private Label nicknameLabel;

    @FXML
    private ImageView profileImage;

    private User user;

    private ChatDAOImpl chatDAO;

    private ObservableList<String> notifications = FXCollections.observableArrayList();

    public UserDashboardController() throws RemoteException {
        super();
    }


    public void setUser(User user) {
        this.user = user;

        nicknameLabel.setText(user.getNickname());

        if (user.getProfile_pic_path() != null) {
            Image image = new Image("file:" + user.getProfile_pic_path());
            profileImage.setImage(image);
        }

        // Initialize ChatDAO
        chatDAO = new ChatDAOImpl(HibernateUtil.getSessionFactory());

        // Initialize RMI service
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            chatService = (ChatService) registry.lookup("chat");

            chatService.registerClient(this, user.getUsername(), user);

            // Update subscribed chats list
            updateSubscribedChats();
        } catch (Exception e) {
            System.err.println("Error connecting to chat service: " + e.getMessage());
            e.printStackTrace();
        }

        // Load chats and notifications
        loadChats();
        loadNotifications();

        // Initialize notification list view
        if (notificationListView != null) {
            notificationListView.setItems(notifications);
        }

        // Show My Chats pane by default
        showChats(null);
    }

    /**
     * Loads sample notifications into the list
     */
    private void loadNotifications() {
        // Clear existing notifications
        notifications.clear();

        // Add sample notifications
        notifications.add("System: Welcome to the Chat Application!");
        notifications.add("Chat: You have been added to a new chat");
        notifications.add("User: New message from admin");

        // Update notification count on button
        notificationCount = notifications.size();
        notifi.setText("🔔 Notifications (" + notificationCount + ")");
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        // Not used in dashboard, so leave empty
    }

    @Override
    public void notifyChatStarted(int chatId, String time) throws RemoteException {
        // Not used in dashboard, so leave empty
    }

    @Override
    public void notifyUserJoined(String nickname, String time) throws RemoteException {
        // Not used in dashboard, so leave empty
    }

    @Override
    public void notifyUserLeft(String nickname, String time) throws RemoteException {
        // Not used in dashboard, so leave empty
    }

    @Override
    public void notifyChatEnded(String time) throws RemoteException {
        // Not used in dashboard, so leave empty
    }

    @Override
    public void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException {
        // You can reuse your existing handler
        //handleAdminStartedChat(chatId, adminName);
    }

    @Override
    public void updateUserOnlineStatus(String username, boolean isOnline) throws RemoteException {
        // Optional: you can show online status here if needed
        //System.out.println("User " + username + " is now " + (isOnline ? "online" : "offline"));
    }

    @Override
    public void receiveDashboardNotification(String message) throws RemoteException {
        Platform.runLater(() -> {
            notifications.add("🔔 " + message);
            incrementNotifications();
        });
    }

    /**
     * Clears all notifications from the list
     */
    @FXML
    void clearNotifications(ActionEvent event) {
        notifications.clear();
        notificationCount = 0;
        notifi.setText("🔔 Notifications");
    }

    /**
     * Refreshes the notifications in the list
     */
    @FXML
    void refreshNotifications(ActionEvent event) {
        loadNotifications();
    }

    /**
     * Loads and displays chats from the database
     */
    private void loadChats() {
        if (allChatsVBox == null || subscribedChatsVBox == null) {
            System.err.println("Chat VBoxes are null. Make sure they're properly initialized in FXML.");
            return;
        }

        try {
            // Clear existing chats
            allChatsVBox.getChildren().clear();
            subscribedChatsVBox.getChildren().clear();

            // Get all chats from the database
            List<Chat> chats = chatDAO.getAllChats();

            // Initialize RMI service if not already initialized
            if (chatService == null) {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    chatService = (ChatService) registry.lookup("chat");
                } catch (Exception e) {
                    System.err.println("Error connecting to chat service: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            updateSubscribedChats();

            if (chats.isEmpty()) {
                Label noAllChatsLabel = new Label("No chats available");
                noAllChatsLabel.setStyle("-fx-text-fill: black;");
                allChatsVBox.getChildren().add(noAllChatsLabel);

                Label noSubscribedChatsLabel = new Label("No subscribed chats");
                noSubscribedChatsLabel.setStyle("-fx-text-fill: black;");
                subscribedChatsVBox.getChildren().add(noSubscribedChatsLabel);
            } else {
                // Add all chats to the all chats section
                for (Chat chat : chats) {
                    createChatItem(chat, false);
                }

                // Add subscribed chats to the subscribed chats section
                boolean hasSubscribedChats = false;
                for (Chat chat : chats) {
                    if (isSubscribed(chat.getChatId())) {
                        createChatItem(chat, true);
                        hasSubscribedChats = true;
                    }
                }

                if (!hasSubscribedChats) {
                    Label noSubscribedChatsLabel = new Label("No subscribed chats");
                    noSubscribedChatsLabel.setStyle("-fx-text-fill: black;");
                    subscribedChatsVBox.getChildren().add(noSubscribedChatsLabel);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chats: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load chats: " + e.getMessage());
        }
    }

    /**
     * Updates the list of subscribed chat IDs
     */
   /* private void updateSubscribedChats() {
        subscribedChatIds.clear();

        if (chatService != null && user != null) {
            try {
                // Get all chats
                List<Chat> chats = chatDAO.getAllChats();

                // Check each chat if the user is subscribed
                for (Chat chat : chats) {
                    List<String> subscribers = chatService.getSubscribedUsers(chat.getChatId());
                    if (subscribers.contains(user.getUsername())) {
                        subscribedChatIds.add(chat.getChatId());
                    }
                }

                // Debug output
                System.out.println("Updated subscribed chats for user " + user.getUsername() + ": " + subscribedChatIds);
            } catch (Exception e) {
                System.err.println("Error getting subscribed chats: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }*/
    private synchronized void updateSubscribedChats() {
        // Create a temporary list to collect the subscribed chat IDs
        List<Integer> tempSubscribedChatIds = new ArrayList<>();

        if (chatService != null && user != null) {
            try {
                List<Chat> chats = chatDAO.getAllChats();
                for (Chat chat : chats) {
                    List<String> subscribers = chatService.getSubscribedUsers(chat.getChatId());
                    if (subscribers.contains(user.getUsername())) {
                        tempSubscribedChatIds.add(chat.getChatId());
                    }
                }

                // Only clear and update the main list once we have collected all the IDs
                subscribedChatIds.clear();
                subscribedChatIds.addAll(tempSubscribedChatIds);

                System.out.println("Updated subscribed chats for user " + user.getUsername() + ": " + subscribedChatIds);
            } catch (Exception e) {
                System.err.println("Error updating subscribed chats: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /*@Override
    public void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException {
        handleAdminStartedChat(chatId, adminName);
    }
*/

    /**
     * Checks if the user is subscribed to a chat
     */
    private boolean isSubscribed(int chatId) {
        return subscribedChatIds.contains(chatId);
    }

    /**
     * Creates a UI element for a chat and adds it to the chat list
     * 
     * @param chat The chat to create an item for
     * @param isSubscribedSection Whether this item is for the subscribed chats section
     */
    private void createChatItem(Chat chat, boolean isSubscribedSection) {
        // Create a container for the chat item
        HBox chatItemContainer = new HBox(10); // 10 is the spacing between elements
        chatItemContainer.setPrefWidth(400);
        chatItemContainer.setPrefHeight(40);
        chatItemContainer.setStyle("-fx-background-color: #F38C79;");

        // Create a button for the chat
        Button chatButton = new Button();
        chatButton.setPrefWidth(200); // Reduced width to make room for additional buttons
        chatButton.setPrefHeight(40);
        chatButton.setStyle("-fx-background-color: #F38C79; -fx-text-fill: white; -fx-font-size: 16px;");

        // Get chat name from the chat object
        String chatName = chat.getName() != null && !chat.getName().isEmpty() ? 
                          chat.getName() : "Chat " + chat.getChatId();

        // For backward compatibility with existing chats
        if (chatName.equals("Chat " + chat.getChatId()) && 
            chat.getFilePath() != null && chat.getFilePath().contains("|")) {
            String[] parts = chat.getFilePath().split("\\|", 2);
            if (parts.length > 0 && !parts[0].isEmpty()) {
                chatName = parts[0];
            }
        }

        chatButton.setText(chatName);

        // Add action to open the chat
        chatButton.setOnAction(event -> openChat(chat));

        // For subscribed section, show Join and Unsubscribe buttons
        // For all chats section, show Subscribe button if not already subscribed
        Button actionButton;
        Button joinButton = null;

        if (isSubscribedSection) {
            // Create join button for subscribed chats
            joinButton = new Button("Join");
            joinButton.setPrefWidth(80);
            joinButton.setPrefHeight(40);
            joinButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

            // Add action to join the chat
            joinButton.setOnAction(event -> openChat(chat));

            // Create unsubscribe button
            actionButton = new Button("Unsubscribe");
            actionButton.setPrefWidth(100);
            actionButton.setPrefHeight(40);
            actionButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

            // Add action to unsubscribe from the chat
            final String finalChatName = chatName; // Make chatName effectively final for use in lambda
            actionButton.setOnAction(event -> {
                try {
                    if (chatService == null) {
                        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                        chatService = (ChatService) registry.lookup("chat");
                    }

                    // Unsubscribe from the chat
                    chatService.unsubscribeFromChat(chat.getChatId(), user.getUsername());

                    // Update subscribed chats list
                    updateSubscribedChats();

                    // Reload chats to update UI
                    loadChats();

                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Unsubscribed", 
                        "You have successfully unsubscribed from " + finalChatName);

                } catch (Exception e) {
                    System.err.println("Error unsubscribing from chat: " + e.getMessage());
                    e.printStackTrace();

                    // Show error message
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to unsubscribe from chat: " + e.getMessage());
                }
            });
        } else {
            // Create subscribe button
            actionButton = new Button("Subscribe");
            actionButton.setPrefWidth(100);
            actionButton.setPrefHeight(40);

            // If already subscribed, disable the button
            if (isSubscribed(chat.getChatId())) {
                actionButton.setDisable(true);
                actionButton.setText("Subscribed");
                actionButton.setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: white;");
            } else {
                actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

                // Add action to subscribe to the chat and open the chat window
                final String finalChatName = chatName; // Make chatName effectively final for use in lambda
                actionButton.setOnAction(event -> {
                    try {
                        if (chatService == null) {
                            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                            chatService = (ChatService) registry.lookup("chat");
                        }

                        chatService.subscribeToChat(chat.getChatId(), user.getUsername());

                        // Re-check subscription and refresh UI after small delay
                        Thread.sleep(200);  // optional but can help with sync issues

                        Platform.runLater(() -> {
                            updateSubscribedChats();  // Refresh list of subscribed chat IDs
                            loadChats();              // Re-render chat lists

                            showAlert(Alert.AlertType.INFORMATION, "Subscribed",
                                "You subscribed to chat: " + finalChatName);

                            // Chat window opening removed as per requirement
                        });

                    } catch (Exception e) {
                        System.err.println("Error subscribing to chat: " + e.getMessage());
                        e.printStackTrace();

                        // Helpful error output
                        String errorMsg = "Failed to subscribe to chat: " + e.getMessage();
                        if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                            errorMsg += "\n\nCheck that your RMI server is running and reachable.";
                        }

                        showAlert(Alert.AlertType.ERROR, "Subscription Error", errorMsg);
                    }
                });
            }
        }

        // Add buttons to the container
        if (joinButton != null) {
            chatItemContainer.getChildren().addAll(chatButton, joinButton, actionButton);
        } else {
            chatItemContainer.getChildren().addAll(chatButton, actionButton);
        }

        // Add the container to the appropriate chat list
        if (isSubscribedSection) {
            subscribedChatsVBox.getChildren().add(chatItemContainer);
        } else {
            allChatsVBox.getChildren().add(chatItemContainer);
        }
    }

    /**
     * Opens a chat window for the selected chat
     */
    private void openChat(Chat chat) {
        try {
            // Load the chat window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/chat_window.fxml"));
            Parent root = loader.load();

            // Get the controller
            ChatWindowController controller = loader.getController();

            // Get chat name from the chat object
            String chatName = chat.getName() != null && !chat.getName().isEmpty() ? 
                              chat.getName() : "Chat " + chat.getChatId();

            // For backward compatibility with existing chats
            if (chatName.equals("Chat " + chat.getChatId()) && 
                chat.getFilePath() != null && chat.getFilePath().contains("|")) {
                String[] parts = chat.getFilePath().split("\\|", 2);
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    chatName = parts[0];
                }
            }

            // Get the RMI service
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ChatService chatService = (ChatService) registry.lookup("chat");

                // Initialize the controller with user, chat service, and chat ID
                controller.setUser(user);
                controller.setChatService(chatService);
                controller.setChatTitle(chatName);
                controller.setChatId(chat.getChatId());

            } catch (Exception e) {
                System.err.println("Error connecting to chat service: " + e.getMessage());
                e.printStackTrace();

                // Show error message with helpful information
                String errorMsg = "Failed to connect to chat service: " + e.getMessage();
                if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                    errorMsg += "\n\nThe RMI server appears to be offline. Please ensure that:\n" +
                            "1. The RMI server is running (start RMIServerLauncher)\n" +
                            "2. The server is accessible on localhost:1099\n" +
                            "3. No firewall is blocking the connection";
                }
                showAlert(Alert.AlertType.ERROR, "Connection Error", errorMsg);
            }

            // Create a new stage for the chat window
            Stage stage = new Stage();
            stage.setTitle(chatName);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening chat: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open chat: " + e.getMessage());
        }
    }

    /**
     * This method has been disabled as chat creation is no longer supported
     */
    @FXML
    void createNewChat(ActionEvent event) {
        throw new UnsupportedOperationException("Chat creation is no longer supported");
    }



    // Show My Chats Pane
    @FXML
    void showChats(ActionEvent event) {
        myChatPane.setVisible(true);
        NotificatinPane.setVisible(false);
        ProfilePane.setVisible(false);
    }

    // Show Notifications Pane
    @FXML
    void showNotifications(ActionEvent event) {
        myChatPane.setVisible(false);
        NotificatinPane.setVisible(true);
        ProfilePane.setVisible(false);
        System.out.println("Showing Notifications...");

        // After user sees notifications, clear
        clearNotifications();
    }

    public void incrementNotifications() {
        notificationCount++;
        notifi.setText("🔔 Notifications (" + notificationCount + ")");
    }

    public void clearNotifications() {
        notificationCount = 0;
        notifi.setText("🔔 Notifications");
    }

    /**
     * Handles notification when an admin starts a chat
     * This method is called by the RMI client when it receives a notifyAdminStartedChat notification
     */
    public void handleAdminStartedChat(int chatId, String adminName) {
        // Add a notification to the notification list
        Platform.runLater(() -> {
            notifications.add("Admin " + adminName + " started a chat with ID: " + chatId);
            incrementNotifications();

            // Update subscribed chats list
            updateSubscribedChats();

            // Refresh the chat list to show the new chat
            loadChats();

            // Show a notification alert
            showAlert(Alert.AlertType.INFORMATION, "New Chat Available", 
                "Admin " + adminName + " has started a new chat. You can subscribe to it from the 'All Available Chats' section.");
        });
    }



    // Show Profile Settings Pane
    @FXML
    void showProfileSettings(ActionEvent event) {
        myChatPane.setVisible(false);
        NotificatinPane.setVisible(false);
        ProfilePane.setVisible(true);
    }

    // Open FileChooser to select an image
    @FXML
    void changeImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(change.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                // Update the profile image in the UI
                Image image = new Image(selectedImageFile.toURI().toString());
                profileImage.setImage(image);

                // Update the user's profile picture path in the database if user is available
                if (user != null) {
                    user.setProfile_pic_path(selectedImageFile.getAbsolutePath());

                    // Update the user in the database
                    UserDAO userDAO = new UserDAOImpl(HibernateUtil.getSessionFactory());
                    userDAO.updateUser(user);

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture updated successfully!");
                }
            } catch (Exception e) {
                System.err.println("Error updating profile image: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile picture: " + e.getMessage());
            }
        }
    }

    // Update the details
    @FXML
    void detailsUpdate(ActionEvent event) {
        if (uname.getText().isEmpty() || pwd.getText().isEmpty() || nickname.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all the fields (Username, Password, Nickname)!");
            return;
        }

        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not found!");
            return;
        }

        try {
            // Update the user object with the new values
            user.setUsername(uname.getText());
            user.setPassword(pwd.getText());
            user.setNickname(nickname.getText());

            if (!email.getText().isEmpty()) {
                user.setEmail(email.getText());
            }

            // Here you would normally update the user in the database
            // For this example, we'll create a UserDAO instance and update the user
            UserDAO userDAO = new UserDAOImpl(HibernateUtil.getSessionFactory());
            userDAO.updateUser(user);

            // Update the UI
            nicknameLabel.setText(user.getNickname());

            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + e.getMessage());
        }
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to log out!");
        alert.setContentText("Are you sure you want to logout?");

        // Show the alert and wait for the user's response
        if (alert.showAndWait().get() == ButtonType.OK) {

            try {
                // Load the Welcome page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/welcome.fxml"));
                Parent root = loader.load();

                // Get the current stage
                Stage stage = (Stage) logoutBtn.getScene().getWindow();

                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
