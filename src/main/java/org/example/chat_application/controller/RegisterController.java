package org.example.chat_application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.chat_application.model.User;

import java.io.File;
import java.io.IOException;

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
    private Button accountbtn;
    private boolean profileCreated = false;
    private String loggedInUserID = "defaultUser"; // Or user ID, depending on how you're storing sessions

    private User currentUser;

    @FXML
    void gotoAcc(ActionEvent event) {
        if (!profileCreated || currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "You must create a profile first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/userDashboard.fxml"));
            Parent root = loader.load();

            // Pass user data
            UserDashboardController controller = loader.getController();
            controller.setUser(currentUser);

            // Show dashboard
            Stage stage = (Stage) accountbtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



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

        try {
            // Get SessionFactory and create UserDAO
            org.hibernate.SessionFactory sessionFactory = org.example.chat_application.util.HibernateUtil.getSessionFactory();
            org.example.chat_application.dao.UserDAO userDAO = new org.example.chat_application.dao.impl.UserDAOImpl(sessionFactory);

            // Check if a user with the same email already exists
            User existingUser = userDAO.getUserByEmail(email.getText());
            if (existingUser != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "A user with this email already exists!");
                return;
            }

            // Create a new User object
            User user = new User();
            user.setUsername(userName.getText());
            user.setEmail(email.getText());
            user.setNickname(nickname.getText());
            user.setPassword(pwd.getText());
            user.setProfile_pic_path(profilepr.getText());
            user.setRole(User.Role.user); // Set default role as user

            // Save user to database
            userDAO.saveUser(user);

            // Set current user and mark profile as created
            currentUser = user;
            profileCreated = true;

            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile created successfully and saved to database!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user: " + e.getMessage());
            e.printStackTrace();
        }
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
