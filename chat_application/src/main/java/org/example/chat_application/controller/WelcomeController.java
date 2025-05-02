package org.example.chat_application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WelcomeController {
    @FXML
    private Button signinbtn;

    @FXML
    private Button signupbtn;

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


