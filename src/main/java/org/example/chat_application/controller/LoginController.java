package org.example.chat_application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.chat_application.dao.UserDAO;
import org.example.chat_application.dao.impl.UserDAOImpl;
import org.example.chat_application.model.User;
import org.example.chat_application.util.HibernateUtil;

import java.io.IOException;

public class LoginController {
    @FXML
    private Button loginbtn;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private Label wronglogin;

    private UserDAO userDAO;

    @FXML
    public void initialize() {
        // Initialize UserDAO
        userDAO = new UserDAOImpl(HibernateUtil.getSessionFactory());

        // Create test users if they don't exist
        createTestUsersIfNeeded();
    }

    private void createTestUsersIfNeeded() {
        try {
            // Check if admin user exists
            User adminUser = userDAO.getUserByEmail("admin@example.com");
            if (adminUser == null) {
                // Create admin user
                adminUser = new User();
                adminUser.setEmail("admin@example.com");
                adminUser.setUsername("admin");
                adminUser.setPassword("admin123");
                adminUser.setNickname("Admin");
                adminUser.setRole(User.Role.admin);
                userDAO.saveUser(adminUser);
                System.out.println("Created admin test user");
            }

            // Check if regular user exists
            User regularUser = userDAO.getUserByEmail("user@example.com");
            if (regularUser == null) {
                // Create regular user
                regularUser = new User();
                regularUser.setEmail("user@example.com");
                regularUser.setUsername("user");
                regularUser.setPassword("user123");
                regularUser.setNickname("User");
                regularUser.setRole(User.Role.user);
                userDAO.saveUser(regularUser);
                System.out.println("Created regular test user");
            }
        } catch (Exception e) {
            System.err.println("Error creating test users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void userLogin(ActionEvent event) throws IOException {
        String userInput = username.getText();
        String pass = password.getText();

        if (userInput.isEmpty() || pass.isEmpty()) {
            wronglogin.setText("Please enter your data");
            return;
        }

        // Authenticate user using the class-level userDAO instance
        User authenticatedUser = userDAO.authenticateUser(userInput, pass);

        if (authenticatedUser != null) {
            wronglogin.setText("Login successful!");

            // Load appropriate dashboard based on user role
            String dashboardPath;
            if (authenticatedUser.getRole() == User.Role.admin) {
                dashboardPath = "/org/example/chat_application/view/admin.fxml";
            } else {
                dashboardPath = "/org/example/chat_application/view/userDashboard.fxml";
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(dashboardPath));
            Parent dashboardRoot = fxmlLoader.load();

            // Pass the user object to the appropriate controller
            if (authenticatedUser.getRole() == User.Role.admin) {
                AdminDashboardController controller = fxmlLoader.getController();
                controller.setUser(authenticatedUser);
            } else {
                UserDashboardController controller = fxmlLoader.getController();
                controller.setUser(authenticatedUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Chat Application - " + (authenticatedUser.getRole() == User.Role.admin ? "Admin" : "User") + " Dashboard");
            stage.setScene(new Scene(dashboardRoot, 900, 600));
            stage.show();
        } else {
            wronglogin.setText("Wrong username or password");
        }
    }


}
