package org.example.chat_application.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.chat_application.dao.impl.UserDAOImpl;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.User;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.util.HibernateUtil;

import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;

public class SubscriptionPopupController {

    @FXML
    private Label chatNameLabel;

    @FXML
    private TableView<UserTableItem> usersTable;

    @FXML
    private TableColumn<UserTableItem, Integer> userIdColumn;

    @FXML
    private TableColumn<UserTableItem, String> usernameColumn;

    @FXML
    private TableColumn<UserTableItem, Button> subscribeColumn;

    @FXML
    private TableView<UserTableItem> subscribedUsersTable;

    @FXML
    private TableColumn<UserTableItem, Integer> subUserIdColumn;

    @FXML
    private TableColumn<UserTableItem, String> subUsernameColumn;

    @FXML
    private TableColumn<UserTableItem, Button> unsubscribeColumn;

    @FXML
    private Button startChatButton;

    @FXML
    private Button closeButton;

    private Chat chat;
    private User adminUser;
    private ChatService chatService;
    private UserDAOImpl userDAO;
    private Consumer<Void> onChatStarted;

    // Observable lists for the tables
    private ObservableList<UserTableItem> allUsers = FXCollections.observableArrayList();
    private ObservableList<UserTableItem> subscribedUsers = FXCollections.observableArrayList();

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        // Initialize the UserDAO
        userDAO = new UserDAOImpl(HibernateUtil.getSessionFactory());

        // Set up the columns for the users table
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        subscribeColumn.setCellValueFactory(new PropertyValueFactory<>("actionButton"));

        // Set up the columns for the subscribed users table
        subUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        subUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        unsubscribeColumn.setCellValueFactory(new PropertyValueFactory<>("actionButton"));

        // Set the items for the tables
        usersTable.setItems(allUsers);
        subscribedUsersTable.setItems(subscribedUsers);
    }

    /**
     * Sets the chat for this popup
     * @param chat The chat to manage subscriptions for
     */
    public void setChat(Chat chat) {
        this.chat = chat;

        // Update the chat name label
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

        chatNameLabel.setText("Chat: " + chatName);

        // Load users only if chatService is also initialized
        if (chatService != null) {
            loadUsers();
        }
    }

    /**
     * Sets the admin user for this popup
     * @param adminUser The admin user
     */
    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    /**
     * Sets the chat service for this popup
     * @param chatService The chat service
     */
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;

        // Load users if chat is already initialized
        if (chat != null) {
            loadUsers();
        }
    }

    /**
     * Sets the callback to be called when the chat is started
     * @param onChatStarted The callback
     */
    public void setOnChatStarted(Consumer<Void> onChatStarted) {
        this.onChatStarted = onChatStarted;
    }

    /**
     * Loads all users and subscribed users
     */
    private void loadUsers() {
        if (chat == null || chatService == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Chat or chat service not initialized");
            return;
        }

        try {
            // Clear the lists
            allUsers.clear();
            subscribedUsers.clear();

            // Get all users
            List<User> users = userDAO.getAllUsers();

            // Get subscribed users
            List<String> subscribedUsernames = chatService.getSubscribedUsers(chat.getChatId());

            // Add users to the appropriate lists
            for (User user : users) {
                boolean isSubscribed = subscribedUsernames.contains(user.getUsername());

                // Create appropriate button based on subscription status
                Button actionButton;
                if (isSubscribed) {
                    // Create a button to unsubscribe
                    Button unsubscribeButton = new Button("Unsubscribe");
                    unsubscribeButton.setOnAction(e -> unsubscribeUser(user));

                    // Add to subscribed users list
                    subscribedUsers.add(new UserTableItem(user.getUser_id(), user.getUsername(), unsubscribeButton));

                    // For the all users table, create a disabled button showing status
                    actionButton = new Button("Subscribed");
                    actionButton.setDisable(true);
                } else {
                    // Create a button to subscribe
                    actionButton = new Button("Subscribe");
                    actionButton.setOnAction(e -> subscribeUser(user));
                }

                // Add to all users list (both subscribed and unsubscribed)
                allUsers.add(new UserTableItem(user.getUser_id(), user.getUsername(), actionButton));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }
    }

    /**
     * Subscribes a user to the chat
     * @param user The user to subscribe
     */
    private void subscribeUser(User user) {
        try {
            // Subscribe the user to the chat
            chatService.subscribeToChat(chat.getChatId(), user.getUsername());

            // Reload the users
            loadUsers();

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "User " + user.getUsername() + " subscribed to chat");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to subscribe user: " + e.getMessage());
        }
    }

    /**
     * Unsubscribes a user from the chat
     * @param user The user to unsubscribe
     */
    private void unsubscribeUser(User user) {
        try {
            // Unsubscribe the user from the chat
            chatService.unsubscribeFromChat(chat.getChatId(), user.getUsername());

            // Reload the users
            loadUsers();

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "User " + user.getUsername() + " unsubscribed from chat");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to unsubscribe user: " + e.getMessage());
        }
    }

    /**
     * Starts the chat
     */
    @FXML
    void startChat(ActionEvent event) {
        if (chat == null || chatService == null || adminUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Chat, chat service, or admin user not initialized");
            return;
        }

        try {
            // Start the chat
            chatService.startChat(chat.getChatId(), adminUser.getUsername());

            // Notify subscribed users
            chatService.notifySubscribedUsers(chat.getChatId(), "Chat started by admin: " + adminUser.getUsername());

            // Call the callback if provided
            if (onChatStarted != null) {
                onChatStarted.accept(null);
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Chat started successfully");

            // Close the popup
            closePopup(event);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to start chat: " + e.getMessage());
        }
    }

    /**
     * Closes the popup
     */
    @FXML
    void closePopup(ActionEvent event) {
        // Get the stage from the button
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Class to represent a user in the table
     */
    public static class UserTableItem {
        private final int userId;
        private final String username;
        private final Button actionButton;

        public UserTableItem(int userId, String username, Button actionButton) {
            this.userId = userId;
            this.username = username;
            this.actionButton = actionButton;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Button getActionButton() {
            return actionButton;
        }
    }
}
