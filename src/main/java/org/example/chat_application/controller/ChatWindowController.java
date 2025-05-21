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
    public Label endTimeLabel;
    @FXML
    public Label startTimeLabel;
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

    private VBox activeUsersBox;
    private Timer activeUsersTimer;

    public ChatWindowController() throws RemoteException {
        super();
    }

    public void initialize() {
        messageField.setDisable(true);
        sendButton.setDisable(true);
        updateSubscriptionUI(false);
        createActiveUsersBox();
    }

    private void createActiveUsersBox() {
        activeUsersBox = new VBox(10);
        activeUsersBox.setPadding(new Insets(10));
        activeUsersBox.setStyle("-fx-background-color: #034C53;");
        activeUsersBox.setPrefWidth(150);

        Label activeUsersLabel = new Label("Active Users");
        activeUsersLabel.setTextFill(Color.WHITE);
        activeUsersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        activeUsersBox.getChildren().add(activeUsersLabel);

        root.setRight(activeUsersBox);
    }

    private void updateActiveUsersList() {
        if (chatService != null && currentChatId > 0) {
            try {
                List<String> activeUsers = chatService.getActiveUsers(currentChatId);
                Platform.runLater(() -> {
                    if (activeUsersBox.getChildren().size() > 1) {
                        activeUsersBox.getChildren().remove(1, activeUsersBox.getChildren().size());
                    }
                    for (String username : activeUsers) {
                        Label userLabel = new Label(username);
                        userLabel.setTextFill(Color.WHITE);
                        activeUsersBox.getChildren().add(userLabel);
                    }
                    updateParticipantCount(activeUsers.size());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSubscriptionUI(boolean subscribed) {
        isSubscribed = subscribed;
        if (subscriptionStatusLabel != null) {
            subscriptionStatusLabel.setText(subscribed ? "Subscribed" : "Not Subscribed");
            subscriptionStatusLabel.setTextFill(subscribed ? Color.GREEN : Color.RED);
        }
        if (subscribeButton != null) subscribeButton.setDisable(subscribed);
        if (unsubscribeButton != null) unsubscribeButton.setDisable(!subscribed);
        messageField.setDisable(!subscribed);
        sendButton.setDisable(!subscribed);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user.getProfile_pic_path() != null && !user.getProfile_pic_path().isEmpty()) {
            this.profileImagePath = user.getProfile_pic_path();
        } else {
            URL imageUrl = getClass().getResource("/org/example/chat_application/images/user1.png");
            this.profileImagePath = imageUrl != null ? imageUrl.toExternalForm() : "";
        }
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setChatId(int chatId) {
        this.currentChatId = chatId;

        try {
            if (chatService != null && currentUser != null) {
                // Register the client for callbacks
                chatService.registerClient(this, currentUser.getUsername(), currentUser);

                // ✅ Let the server handle the "joined" notification and logging
                chatService.userJoinedChat(chatId, currentUser.getUsername());

                // ✅ UI updates
                updateSubscriptionUI(true); // Assume user is subscribed if they opened the window
                startActiveUsersTimer();
                updateActiveUsersList();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            addEventMessage("Error initializing chat: " + e.getMessage());
        }
    }


    private void startActiveUsersTimer() {
        if (activeUsersTimer != null) activeUsersTimer.cancel();
        activeUsersTimer = new Timer();
        activeUsersTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateActiveUsersList();
            }
        }, 5000, 5000);
    }

    @FXML
    void subscribeToChat(ActionEvent event) {
        try {
            if (chatService != null && !isSubscribed) {
                chatService.subscribeToChat(currentChatId, currentUser.getUsername());
                updateSubscriptionUI(true);
                addEventMessage("You have subscribed to this chat.");
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
                chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());
                updateSubscriptionUI(false);
                addEventMessage("You have unsubscribed from this chat.");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            addEventMessage("Error unsubscribing: " + e.getMessage());
        }
    }


    public void setChatTitle(String title) {
        if (chatRoomLabel != null) chatRoomLabel.setText(title);
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
                if (!isSubscribed) {
                    addEventMessage("You must be subscribed to send messages.");
                    return;
                }

                // ❌ Do NOT call addChatMessage manually here anymore

                chatService.broadcastMessage(message, currentUser.getUsername());

                // Optional: Handle "Bye" to auto-leave
                if (message.equalsIgnoreCase("Bye")) {
                    chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());
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
            if (activeUsersTimer != null) activeUsersTimer.cancel();
            if (chatService != null && isSubscribed) {
                chatService.unsubscribeFromChat(currentChatId, currentUser.getUsername());
                isSubscribed = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });
    }

    // === RMI Callbacks ===

    /*@Override
    public void receiveMessage(String message) throws RemoteException {
        Platform.runLater(() -> {
            if (message.startsWith("SYSTEM: ")) {
                addEventMessage(message.substring(8));
            } else {
                int idx = message.indexOf(": ");
                if (idx > 0) {
                    String sender = message.substring(0, idx);
                    String content = message.substring(idx + 2);
                    URL imgUrl = getClass().getResource("/org/example/chat_application/images/user1.png");
                    String profileImg = imgUrl != null ? imgUrl.toExternalForm() : "";
                    addChatMessage(sender, content, profileImg);
                } else {
                    addEventMessage(message);
                }
            }
        });
    }*/
    @Override
    public void receiveMessage(String message) throws RemoteException {
        Platform.runLater(() -> {
            if (message.startsWith("SYSTEM: ")) {
                addEventMessage(message.substring(8));
            } else {
                String[] parts = message.split("###");
                if (parts.length == 3) {
                    String sender = parts[0];
                    String imagePath = parts[1];
                    String content = parts[2];
                    addChatMessage(sender, content, imagePath);
                } else {
                    // fallback in case formatting fails
                    int idx = message.indexOf(": ");
                    if (idx > 0) {
                        String sender = message.substring(0, idx);
                        String content = message.substring(idx + 2);
                        addChatMessage(sender, content, ""); // empty/default image
                    } else {
                        addEventMessage(message);
                    }
                }
            }
        });
    }


    @Override
    public void notifyChatStarted(int chatId, String time) throws RemoteException {
        Platform.runLater(() -> addEventMessage("Chat started at: " + time));
    }

    @Override
    public void notifyUserJoined(String nickname, String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("\"" + nickname + "\" has joined: " + time);
            try {
                int count = chatService.getSubscribedUsers(currentChatId).size();
                updateParticipantCount(count);
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
                int count = chatService.getSubscribedUsers(currentChatId).size();
                updateParticipantCount(count);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    //using thread Updates the client UI upon chat termination
    public void notifyChatEnded(String time) throws RemoteException {
        Platform.runLater(() -> {
            addEventMessage("Chat stopped at: " + time);
            messageField.setDisable(true);
            sendButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    closeChat();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }



    @Override
    public void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException {
        Platform.runLater(() -> addEventMessage("Admin " + adminName + " started a chat. Join to participate."));
    }

    @Override
    public void updateUserOnlineStatus(String username, boolean isOnline) throws RemoteException {

    }
    @Override
    public void receiveDashboardNotification(String message) throws RemoteException {
        // ChatWindowController doesn't use dashboard notifications, so we can leave it empty
        System.out.println("Received dashboard notification (ignored in ChatWindow): " + message);
    }


}
