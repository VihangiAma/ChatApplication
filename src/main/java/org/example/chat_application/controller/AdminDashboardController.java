package org.example.chat_application.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.chat_application.dao.impl.ChatDAOImpl;
import org.example.chat_application.dao.impl.UserDAOImpl;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.User;
import org.example.chat_application.controller.NewChatController;
import org.example.chat_application.controller.SubscriptionPopupController;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.util.AlertUtil;
import org.example.chat_application.util.HibernateUtil;
import org.hibernate.SessionFactory;
import javafx.concurrent.Task;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Date;


public class AdminDashboardController implements javafx.fxml.Initializable {
    @FXML
    private Button logoutBtn;

    // Old logout image view, replaced with button
    // @FXML
    // private ImageView Logout;

    @FXML
    private AnchorPane ManageUser;

    @FXML
    private Button NewChatBtn;

    @FXML
    private AnchorPane Notification;

    @FXML
    private AnchorPane Setting;

    @FXML
    private AnchorPane Welcome;

    @FXML
    private Button chatButton;

    @FXML
    private AnchorPane creatchat;

    @FXML
    private Button notifiButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button userButton;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nicknameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> actionColumn;

    @FXML
    private TableView<User> userTable;

    @FXML
    private Label adminNameLabel;

    @FXML
    private ImageView adminProfileImage;

    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label nickNameLabel;

    private final UserDAOImpl userDAO;
    private final ChatDAOImpl chatDAO;

    private User adminUser;

    private File selectedImageFile;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox chatListVBox;

    @FXML
    private TableView<Notification> notificationTable;

    @FXML
    private TableColumn<Notification, String> notifDateColumn;

    @FXML
    private TableColumn<Notification, String> notifTypeColumn;

    @FXML
    private TableColumn<Notification, String> notifMessageColumn;

    @FXML
    private Button clearNotificationsBtn;

    @FXML
    private Button refreshNotificationsBtn;

    // Simple notification class for the table
    public static class Notification {
        private final String date;
        private final String type;
        private final String message;

        public Notification(String date, String type, String message) {
            this.date = date;
            this.type = type;
            this.message = message;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public String getMessage() { return message; }
    }

    private ObservableList<Notification> notifications = FXCollections.observableArrayList();

    // Add a constructor to initialize userDAO properly
    public AdminDashboardController() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.userDAO = new UserDAOImpl(sessionFactory);
        this.chatDAO = new ChatDAOImpl(sessionFactory);
    }

    /**
     * Sets the admin user and updates the UI with their information
     * @param user The authenticated admin user
     */
    public void setUser(User user) {
        this.adminUser = user;

        // Update UI with admin information if the UI elements are available
        if (adminNameLabel != null) {
            adminNameLabel.setText(user.getUsername());
        }

        if (nickNameLabel != null) {
            nickNameLabel.setText(user.getNickname());
        }

        // Set profile image if available
        if (adminProfileImage != null && user.getProfile_pic_path() != null) {
            try {
                Image image = new Image("file:" + user.getProfile_pic_path());
                adminProfileImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }

        // Update profile settings pane
        updateProfileSettings();
    }

    /**
     * Updates the profile settings pane with admin user information
     */
    private void updateProfileSettings() {
        if (adminUser == null) return;

        // Update profile information labels
        if (firstNameLabel != null && lastNameLabel != null && nickNameLabel != null) {
            // Split username into first and last name if possible
            String username = adminUser.getUsername();
            String[] nameParts = username.split(" ", 2);

            if (nameParts.length > 0) {
                firstNameLabel.setText(nameParts[0]);
            }

            if (nameParts.length > 1) {
                lastNameLabel.setText(nameParts[1]);
            } else {
                lastNameLabel.setText("");
            }

            nickNameLabel.setText(adminUser.getNickname());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupNotificationTable();
        loadUserData();
        loadChatData();
        loadNotifications();

        // Show welcome pane by default
        hideAllPanes();
        Welcome.setVisible(true);
    }

    /**
     * Sets up the notification table columns
     */
    private void setupNotificationTable() {
        notifDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        notifTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        notifMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));

        notificationTable.setItems(notifications);
    }

    /**
     * Loads sample notifications into the table
     */
    private void loadNotifications() {
        // Clear existing notifications
        notifications.clear();

        // Add sample notifications
        notifications.add(new Notification(new Date().toString(), "System", "Welcome to the Admin Dashboard"));
        notifications.add(new Notification(new Date().toString(), "User", "New user registered"));
        notifications.add(new Notification(new Date().toString(), "Chat", "New chat created"));

        // Refresh the table
        notificationTable.refresh();
    }

    /**
     * Clears all notifications from the table
     */
    @FXML
    void clearNotifications(ActionEvent event) {
        notifications.clear();
        notificationTable.refresh();
    }

    /**
     * Refreshes the notifications in the table
     */
    @FXML
    void refreshNotifications(ActionEvent event) {
        loadNotifications();
    }


    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        nicknameColumn.setCellValueFactory(new PropertyValueFactory<>("nickname"));

        // Setup action column with buttons
        actionColumn.setCellFactory(column -> {
            return new TableCell<User, String>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

                {
                    // Edit button action
                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        openEditUserDialog(user);
                    });

                    // Delete button action
                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        deleteUser(user);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            };
        });
    }

    private void loadUserData() {
        System.out.println("Loading user data...");
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                try {
                    List<User> users = userDAO.getAllUsers();
                    System.out.println("Retrieved " + users.size() + " users from database");
                    for (User user : users) {
                        System.out.println("User: ID=" + user.getUser_id() + 
                                          ", Username=" + user.getUsername() + 
                                          ", Email=" + user.getEmail() + 
                                          ", Role=" + user.getRole() + 
                                          ", Nickname=" + user.getNickname());
                    }
                    return users;
                } catch (Exception e) {
                    System.err.println("Error retrieving users: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>(); // Return empty list as fallback
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<User> userList = task.getValue();
            System.out.println("Task succeeded, setting " + userList.size() + " users to table");
            ObservableList<User> data = FXCollections.observableArrayList(userList);
            userTable.setItems(data);
            System.out.println("Table items set, item count: " + userTable.getItems().size());
        });

        task.setOnFailed(e -> {
            System.err.println("Task failed: " + task.getException().getMessage());
            task.getException().printStackTrace();
            // Show error dialog to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load users: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
        System.out.println("User data loading task started");
    }

    private void loadChatData() {
        System.out.println("Loading chat data...");
        Task<List<Chat>> task = new Task<>() {
            @Override
            protected List<Chat> call() throws Exception {
                try {
                    List<Chat> chats = chatDAO.getAllChats();
                    System.out.println("Retrieved " + chats.size() + " chats from database");
                    return chats;
                } catch (Exception e) {
                    System.err.println("Error retrieving chats: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>(); // Return empty list as fallback
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Chat> chatList = task.getValue();
            System.out.println("Task succeeded, displaying " + chatList.size() + " chats");
            displayChats(chatList);
        });

        task.setOnFailed(e -> {
            System.err.println("Task failed: " + task.getException().getMessage());
            task.getException().printStackTrace();
            // Show error dialog to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load chats: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
        System.out.println("Chat data loading task started");
    }

    private void displayChats(List<Chat> chats) {
        if (chatListVBox == null) {
            System.err.println("chatListVBox is null. Make sure it's properly initialized in FXML.");
            return;
        }

        chatListVBox.getChildren().clear();

        if (chats.isEmpty()) {
            Label noChatsLabel = new Label("No chats available");
            noChatsLabel.setStyle("-fx-text-fill: white;");
            chatListVBox.getChildren().add(noChatsLabel);
        } else {
            for (Chat chat : chats) {
                HBox chatBox = createChatBox(chat);
                chatListVBox.getChildren().add(chatBox);
            }
        }
    }

    private HBox createChatBox(Chat chat) {
        HBox chatBox = new HBox();
        chatBox.setSpacing(10);
        chatBox.setPadding(new Insets(10));
        chatBox.setStyle("-fx-background-color: #1e88e5; -fx-background-radius: 5;");

        VBox infoBox = new VBox();
        infoBox.setSpacing(5);

        // Get chat name and description from the chat object
        String chatName = chat.getName() != null && !chat.getName().isEmpty() ? 
                          chat.getName() : "Chat " + chat.getChatId();
        String description = chat.getDescription() != null ? chat.getDescription() : "";

        // For backward compatibility with existing chats
        if ((chatName.equals("Chat " + chat.getChatId()) || description.isEmpty()) && 
            chat.getFilePath() != null && chat.getFilePath().contains("|")) {
            String[] parts = chat.getFilePath().split("\\|", 2);
            if (parts.length > 0 && !parts[0].isEmpty()) {
                chatName = parts[0];
            }
            if (parts.length > 1) {
                description = parts[1];
            }
        }

        Label nameLabel = new Label(chatName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label idLabel = new Label("ID: " + chat.getChatId());
        idLabel.setStyle("-fx-text-fill: white;");

        Label dateLabel = new Label("Started: " + (chat.getStartedAt() != null ? chat.getStartedAt().toString() : "N/A"));
        dateLabel.setStyle("-fx-text-fill: white;");

        infoBox.getChildren().addAll(nameLabel, idLabel, dateLabel);

        // Add description if available
        if (!description.isEmpty()) {
            Label descLabel = new Label("Description: " + description);
            descLabel.setStyle("-fx-text-fill: white;");
            descLabel.setWrapText(true);
            infoBox.getChildren().add(descLabel);
        }

        Button openButton = new Button("Open");
        openButton.setOnAction(event -> {
            // Handle opening the chat
            System.out.println("Opening chat: " + chat.getChatId());
            openSubscriptionPopup(chat);
        });

        chatBox.getChildren().addAll(infoBox, openButton);

        return chatBox;
    }



    // Hide all panels
    private void hideAllPanes() {
        ManageUser.setVisible(false);
        creatchat.setVisible(false);
        Notification.setVisible(false);
        Setting.setVisible(false);
        Welcome.setVisible(false);
    }

    @FXML
    void Creat_New_Chat(ActionEvent event) {
        try {
            // Load the Chat_Form.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/Chat_Form.fxml"));
            Parent root = loader.load();

            // Get the controller
            NewChatController controller = loader.getController();

            // Pass the current admin user to the controller
            controller.setAdminUser(adminUser);

            // Set the callback to reload chat data when a chat is created
            controller.setOnChatCreated(() -> {
                loadChatData();

                // Start the chat in the RMI service if available
                try {
                    // Get the RMI service
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    ChatService chatService = (ChatService) registry.lookup("chat");

                    // Start the chat with the most recently created chat ID
                    int chatId = controller.getLastCreatedChatId();
                    if (chatId > 0) {
                        chatService.startChat(chatId, adminUser.getUsername());

                        // Notify all users about the new chat
                        chatService.notifyAllUsersAboutNewChat(chatId, adminUser.getUsername());

                        // Show success message
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Chat Started");
                            alert.setHeaderText(null);
                            alert.setContentText("Chat has been started and all users have been notified.");
                            alert.showAndWait();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error starting chat in RMI service: " + e.getMessage());
                    e.printStackTrace();

                    // Prepare error message with helpful information
                    String errorMsg = "Failed to start chat in RMI service: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                        errorMsg += "\n\nThe RMI server appears to be offline. Please ensure that:\n" +
                                "1. The RMI server is running (start RMIServerLauncher)\n" +
                                "2. The server is accessible on localhost:1099\n" +
                                "3. No firewall is blocking the connection";
                    }

                    // Show error message
                    final String finalErrorMsg = errorMsg;
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Connection Error");
                        alert.setHeaderText(null);
                        alert.setContentText(finalErrorMsg);
                        alert.showAndWait();
                    });
                }
            });

            // Create a new stage for the form
            Stage stage = new Stage();
            stage.setTitle("Create New Chat");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Show the form and wait for it to close
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading Chat_Form.fxml: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open chat creation form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void create_chat(ActionEvent event) {
        hideAllPanes();
        creatchat.setVisible(true); // Shows the creation chat pane

        // Reload chat data to ensure the list is up-to-date
        loadChatData();
    }

    @FXML
    void manage_user(ActionEvent event) {
        hideAllPanes();
        ManageUser.setVisible(true); // Shows the manage user pane
    }

    @FXML
    void notification(ActionEvent event) {
        hideAllPanes();
        Notification.setVisible(true); // Shows the notifications pane
    }

    @FXML
    void setting(ActionEvent event) {
        hideAllPanes();
        Setting.setVisible(true); // Shows the profile/settings pane
    }

    @FXML
    void changeProfileImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(((Button)event.getSource()).getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                // Update the profile image in the UI
                Image image = new Image(selectedImageFile.toURI().toString());
                adminProfileImage.setImage(image);

                // Update the user's profile picture path in the database
                if (adminUser != null) {
                    adminUser.setProfile_pic_path(selectedImageFile.getAbsolutePath());
                    userDAO.updateUser(adminUser);

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Profile picture updated successfully!");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("Error updating profile image: " + e.getMessage());
                e.printStackTrace();

                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update profile picture: " + e.getMessage());
                alert.showAndWait();
            }
        }
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

    /**
     * Opens a dialog to edit a user's information
     * @param user The user to edit
     */
    private void openEditUserDialog(User user) {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit User");
            dialog.setHeaderText("Edit user information for: " + user.getUsername());

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField usernameField = new TextField(user.getUsername());
            TextField emailField = new TextField(user.getEmail());
            TextField nicknameField = new TextField(user.getNickname() != null ? user.getNickname() : "");
            PasswordField passwordField = new PasswordField();
            ComboBox<User.Role> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll(User.Role.values());
            roleComboBox.setValue(user.getRole());

            grid.add(new Label("Username:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(emailField, 1, 1);
            grid.add(new Label("Nickname:"), 0, 2);
            grid.add(nicknameField, 1, 2);
            grid.add(new Label("Password:"), 0, 3);
            grid.add(passwordField, 1, 3);
            grid.add(new Label("Role:"), 0, 4);
            grid.add(roleComboBox, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the username field by default
            Platform.runLater(() -> usernameField.requestFocus());

            // Show the dialog and wait for the result
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButtonType) {
                // Update the user object with the new values
                user.setUsername(usernameField.getText());
                user.setEmail(emailField.getText());
                user.setNickname(nicknameField.getText());
                if (!passwordField.getText().isEmpty()) {
                    user.setPassword(passwordField.getText());
                }
                user.setRole(roleComboBox.getValue());

                try {
                    // Update the user in the database
                    userDAO.updateUser(user);

                    // Refresh the table
                    loadUserData();

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("User updated successfully!");
                    alert.showAndWait();
                } catch (Exception e) {
                    System.err.println("Error updating user: " + e.getMessage());
                    e.printStackTrace();

                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to update user: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening edit dialog: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open edit dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Opens the subscription popup for a chat
     * @param chat The chat to manage subscriptions for
     */
    private void openSubscriptionPopup(Chat chat) {
        try {
            // Load the subscription popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/subscription_popup.fxml"));
            Parent root = loader.load();

            // Get the controller
            SubscriptionPopupController controller = loader.getController();

            // Initialize the controller with chat, admin user, and chat service
            controller.setChat(chat);
            controller.setAdminUser(adminUser);

            // Get the RMI service
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ChatService chatService = (ChatService) registry.lookup("chat");
                controller.setChatService(chatService);

                // Set callback for when chat is started
                controller.setOnChatStarted(result -> {
                    // Reload chat data
                    loadChatData();

                    // Add notification
                    notifications.add(new Notification(new Date().toString(), "Chat", 
                        "Chat " + chat.getChatId() + " started by admin"));
                    notificationTable.refresh();
                });

            } catch (Exception e) {
                System.err.println("Error connecting to chat service: " + e.getMessage());
                e.printStackTrace();

                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to connect to chat service: " + e.getMessage());
                alert.showAndWait();
                return;
            }

            // Create a new stage for the popup
            Stage stage = new Stage();
            stage.setTitle("Manage Chat Subscriptions");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Show the popup and wait for it to close
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading subscription popup: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open subscription popup: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void deleteUser(User user) {
        try {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete User");
            confirmAlert.setContentText("Are you sure you want to delete user: " + user.getUsername() + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Delete the user from the database
                    userDAO.deleteUser(user.getUser_id());

                    // Refresh the table
                    loadUserData();

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("User deleted successfully!");
                    alert.showAndWait();
                } catch (Exception e) {
                    System.err.println("Error deleting user: " + e.getMessage());
                    e.printStackTrace();

                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to delete user: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error showing delete confirmation: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to show delete confirmation: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleClicks(ActionEvent event) {
        // Get the source of the event (the button that was clicked)
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();

        // Handle different button clicks based on their ID
        switch (buttonId) {
            case "CreatChatBtn":
                create_chat(event);
                break;
            case "ManageUserBtn":
                manage_user(event);
                break;
            case "NotificationBtn":
                notification(event);
                break;
            case "LogoutBtn":
                // Call logout directly without MouseEvent
                // Since we can't easily create a MouseEvent, let's modify the logout method
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
                        Stage stage = (Stage) clickedButton.getScene().getWindow();

                        // Set the new scene
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                System.out.println("Unknown button clicked: " + buttonId);
                break;
        }
    }
}
