package org.example.chat_application.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {
    @FXML
    private Button signinbtn;

    @FXML
    private Button signupbtn;

    @FXML
    private VBox mainContent;

    @FXML
    private Label welcomeTitle;

    @FXML
    private StackPane profileContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initial state - set opacity to 0 for fade-in effect
        mainContent.setOpacity(0);

        // Create fade-in animation for the main content
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Create bounce animation for the welcome title
        TranslateTransition titleBounce = new TranslateTransition(Duration.seconds(0.8), welcomeTitle);
        titleBounce.setFromY(-20);
        titleBounce.setToY(0);
        titleBounce.setCycleCount(1);
        titleBounce.setAutoReverse(false);
        titleBounce.play();
    }

    @FXML
    void gotoSignIn(ActionEvent event) {
        try {
            Parent signInRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/chat_application/view/login.fxml")));
            Scene signInScene = new Scene(signInRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(signInScene);
            stage.setTitle("Sign In");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void gotoSignUp(ActionEvent event) {
        try {
            Parent signUpRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/chat_application/view/register.fxml")));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Create New Account");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
