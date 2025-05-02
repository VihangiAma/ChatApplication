package org.example.chat_application.controller;

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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class UserDashboardController {

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
    private ImageView logout;

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

    private File selectedImageFile;
    private int notificationCount = 2;

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
            Image image = new Image(selectedImageFile.toURI().toString());
            logout.setImage(image); // Assuming you display the selected image in the logout ImageView
        }
    }

    // Update the details
    @FXML
    void detailsUpdate(ActionEvent event) {
        if (uname.getText().isEmpty() || pwd.getText().isEmpty() || nickname.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all the fields (Username, Password, Nickname)!");
            return;
        }

        // Here you would normally update the database or user session details
        // Since you requested just the message, we'll simulate it.

        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
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
    void logout(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to log out!");
        alert.setContentText("Are you sure you want to logout?");

        // Show the alert and wait for the user's response
        if (alert.showAndWait().get() == ButtonType.OK) {

            try {
                // Load the Welcome page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/Welcome.fxml"));
                Parent root = loader.load();

                // Get the current stage
                Stage stage = (Stage) logout.getScene().getWindow();

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



