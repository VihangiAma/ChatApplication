package org.example.chat_application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class RegisterController {

    @FXML
    private PasswordField conpwd;

    @FXML
    private TextField email;

    @FXML
    private TextField nickname;

    @FXML
    private TextField profilepr;

    @FXML
    private PasswordField pwd;

    @FXML
    private Button signup;

    @FXML
    private TextField userName;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button uploadBtn;

    @FXML
    void userSignup(ActionEvent event) {
        if (userName.getText().isEmpty() ||
                email.getText().isEmpty() ||
                nickname.getText().isEmpty() ||
                profilepr.getText().isEmpty() ||
                pwd.getText().isEmpty() ||
                conpwd.getText().isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields!");
            return;
        }

        if (!pwd.getText().equals(conpwd.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
            return;
        }

        if (!isValidImagePath(profilepr.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid image path!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile created successfully!");
    }

    @FXML
    void uploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            profilepr.setText(file.getAbsolutePath());
            Image image = new Image(file.toURI().toString());
            profileImageView.setImage(image);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);  // No header
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidImagePath(String path) {
        return path.toLowerCase().endsWith(".jpg") ||
                path.toLowerCase().endsWith(".jpeg") ||
                path.toLowerCase().endsWith(".png") ||
                path.toLowerCase().endsWith(".gif");
    }
}
