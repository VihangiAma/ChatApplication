package org.example.chat_application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.chat_application.model.User;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientInterface;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatWindowController extends UnicastRemoteObject implements ClientInterface {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    @FXML
    private BorderPane root;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button sendButton;

    @FXML
    private Label chatRoomLabel;

    @FXML
    private Label participantsLabel;

    @FXML
    private Label subscriptionStatusLabel;

    @FXML
    private Button subscribeButton;

    @FXML
    private Button unsubscribeButton;

    private User currentUser;
    private ChatService chatService;
    private int currentChatId;
    private String profileImagePath;
    private boolean isSubscribed = false;

    // For active users display
    private VBox activeUsersBox;
    private Timer activeUsersTimer;

    public ChatWindowController() throws RemoteException {
        super();
    }

    public void initialize() {
        // Disable message field until chat is joined
        messageField.setDisable(true);
        sendButton.setDisable(true);

        // Set initial subscription status
        updateSubscriptionUI(false);

        // Create and add active users box
        createActiveUsersBox();
    }

    /**
     * Creates and adds the active users box to the UI
     */
    private void createActiveUsersBox() {
        // Create a VBox for active users
        activeUsersBox = new VBox(10);
        activeUsersBox.setPadding(new Insets(10));
        activeUsersBox.setStyle("-fx-background-color: #034C53;");
        activeUsersBox.setPrefWidth(150);

        // Add a label for the active users section
        Label activeUsersLabel = new Label("Active Users");
        activeUsersLabel.setTextFill(Color.WHITE);
        activeUsersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        activeUsersBox.getChildren().add(activeUsersLabel);

        // Add the active users box to the right side of the root BorderPane
        root.setRight(activeUsersBox);
    }

    /**
     * Fetches and updates the list of active users in the chat
     */
    private void updateActiveUsersList() {
        if (chatService != null && currentChatId > 0) {
            try {
                // Get active users from the chat service
                List<String> activeUsers = chatService.getActiveUsers(currentChatId);

                // Update the UI on the JavaFX application thread
                Platform.runLater(() -> {
                    // Clear existing users (except the label)
                    if (activeUsersBox.getChildren().size() > 1) {
                        activeUsersBox.getChildren().remove(1, activeUsersBox.getChildren().size());
                    }

                    // Add each active user to the list
                    for (String username : activeUsers) {
                        Label userLabel = new Label(username);
                        userLabel.setTextFill(Color.WHITE);
                        activeUsersBox.getChildren().add(userLabel);
                    }

                    // Update participant count
                    updateParticipantCount(activeUsers.size());
                });
            } catch (Exception e) {
                System.err.println("Error updating active users list: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the UI elements based on the subscription status
     * 
     * @param subscribed true if the user is subscribed, false otherwise
     */
    private void updateSubscriptionUI(boolean subscribed) {
        isSubscribed = subscribed;

        // Update subscription status label
        if (subscriptionStatusLabel != null) {
            if (subscribed) {
                subscriptionStatusLabel.setText("Subscribed");
                subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#4CAF50"));
            } else {
                subscriptionStatusLabel.setText("Not Subscribed");
                subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#ff9999"));
            }
        }

        // Update buttons
        if (subscribeButton != null) {
            subscribeButton.setDisable(subscribed);
        }
        if (unsubscribeButton != null) {
            unsubscribeButton.setDisable(!subscribed);
        }

        // Update message field and send button
        messageField.setDisable(!subscribed);
        sendButton.setDisable(!subscribed);
    }

    public void setUser(User user) {
        this.currentUser = user;

        // Set profile image based on user's profile_pic_path
        if (user.getProfile_pic_path() != null && !user.getProfile_pic_path().isEmpty()) {
            this.profileImagePath = user.getProfile_pic_path();
        } else {
            // Default profile image
            URL imageUrl = getClass().getResource("/org/example/chat_application/images/user1.png");
            if (imageUrl != null) {
                this.profileImagePath = imageUrl.toExternalForm();
            }
        }
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setChatId(int chatId) {
        this.currentChatId = chatId;
        try {
            // Subscribe to the chat
            chatService.subscribeToChat(chatId, currentUser.getUsername());

            // Update UI for subscribed state
            updateSubscriptionUI(true);

            // Start timer to update active users list
            startActiveUsersTimer();

            // Initial update of active users list
            updateActiveUsersList();

            // Add event message
            addEventMessage("You have joined the chat and are now subscribed.");
        } catch (RemoteException e) {
            e.printStackTrace();
            addEventMessage("Error joining chat: " + e.getMessage());
        }
    }

    /**
     * Starts a timer to periodically update the active users list
     */
    private void startActiveUsersTimer() {
        // Cancel existing timer if any
        if (activeUsersTimer != null) {
            activeUsersTimer.cancel();
        }

        // Create a new timer
        activeUsersTimer = new Timer();

        // Schedule a task to update the active users list every 5 seconds
        activeUsersTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateActiveUsersList();
            }
        }, 5000, 5000);
    }

    @FXML
    void subscribeToChat(ActionEvent event) {
        try {
            if (chatService != null && !isSubscribed) {
                // Subscribe to the chat
                chatService.subscribeToChat(currentChatId, currentUser.getUsername());

                // Update UI for subscribed state
                updateSubscriptionUI(true);

                // Add event message
                addEventMessage("You have subscribed to this chat and can now send messages.");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            addEventMessage("Error subscribing to chat: " + e.getMessage());
        }
    }

    @FXML
    void unsubscribeFromChat(ActionEvent event) {
        try {
            if (chatService != null && isSubscribed) {
                // Unsubscribe from the chat
                chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());

                // Update UI for unsubscribed state
                updateSubscriptionUI(false);

                // Add event message
                addEventMessage("You have unsubscribed from this chat. You can still view messages but cannot send any.");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            addEventMessage("Error unsubscribing from chat: " + e.getMessage());
        }
    }

    public void setChatTitle(String title) {
        if (chatRoomLabel != null) {
            chatRoomLabel.setText(title);
        }
    }

    public void updateParticipantCount(int count) {
        if (participantsLabel != null) {
            participantsLabel.setText("Online - " + count + " participants");
        }
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Check if user is subscribed before sending message
                if (!isSubscribed) {
                    addEventMessage("You must be subscribed to this chat to send messages. Please subscribe first.");
                    return;
                }

                // Add message to local chat
                String nickname = currentUser.getNickname() != null ? 
                        currentUser.getNickname() : currentUser.getUsername();
                addChatMessage(nickname, message, profileImagePath);

                // Send message to server
                chatService.broadcastMessage(message, currentUser.getUsername());

                // Check if this is a "Bye" message to leave the chat
                if (message.equalsIgnoreCase("Bye")) {
                    // Unsubscribe from the chat
                    chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());

                    // Disable message field
                    messageField.setDisable(true);
                    sendButton.setDisable(true);
                    isSubscribed = false;
                }

                messageField.clear();
            } catch (RemoteException e) {
                e.printStackTrace();
                addEventMessage("Error sending message: " + e.getMessage());
            }
        }
    }

    private void addChatMessage(String name, String message, String profilePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/chat_bubble.fxml"));
            Node node = loader.load();

            ChatBubbleController controller = loader.getController();
            controller.setData(name, message, profilePath);

            Platform.runLater(() -> {
                chatBox.getChildren().add(node);
                scrollPane.setVvalue(1.0);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEventMessage(String text) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/event_bubble.fxml"));
            Node node = loader.load();

            EventBubbleController controller = loader.getController();
            controller.setText(text);

            Platform.runLater(() -> {
                chatBox.getChildren().add(node);
                scrollPane.setVvalue(1.0);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeChat() {
        try {
            // Cancel the active users timer
            if (activeUsersTimer != null) {
                activeUsersTimer.cancel();
                activeUsersTimer = null;
            }

            // Only unsubscribe if the user is currently subscribed
            if (chatService != null && isSubscribed) {
                chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());
                isSubscribed = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Close the window
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });
    }

    // ClientInterface implementation

    @Override
    public void receiveMessage(String message) throws RemoteException {
        Platform.runLater(() -> {
            // Check if this is a system message
            if (message.startsWith("SYSTEM: ")) {
                addEventMessage(message.substring(8));
            } else {
                // Parse the message to extract sender and content
                int colonIndex = message.indexOf(": ");
                if (colonIndex > 0) {
                    String sender = message.substring(0, colonIndex);
                    String content = message.substring(colonIndex + 2);

                    // Get profile image for sender (use default for now)
                    URL imageUrl = getClass().getResource("/org/example/chat_application/images/user1.png");
                    String profileImage = imageUrl != null ? imageUrl.toExternalForm() : "";

                    addChatMessage(sender, content, profileImage);
                } else {
                    addEventMessage(message);
                }
            }
        });
    }

    @Override
    public void notifyChatStarted(int chatId, String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("Chat started at: " + time);
        });
    }

    @Override
    public void notifyUserJoined(String nickname, String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("\"" + nickname + "\" has joined: " + time);

            try {
                // Update participant count
                if (chatService != null) {
                    int count = chatService.getSubscribedUsers(currentChatId).size();
                    updateParticipantCount(count);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void notifyUserLeft(String nickname, String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("\"" + nickname + "\" left: " + time);

            try {
                // Update participant count
                if (chatService != null) {
                    int count = chatService.getSubscribedUsers(currentChatId).size();
                    updateParticipantCount(count);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void notifyChatEnded(String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("Chat stopped at: " + time);

            // Disable message field
            messageField.setDisable(true);
            sendButton.setDisable(true);

            // Close the chat window after a delay
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // 3 seconds delay
                    closeChat();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    @Override
    public void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("Admin " + adminName + " started a chat. Join to participate.");
        });
    }
}
