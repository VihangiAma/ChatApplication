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

    @FXML
    void userLogin(ActionEvent event)throws IOException {
        String user = username.getText();
        String pass = password.getText();

        if (user.equals("admin") && pass.equals("1234")) {
            wronglogin.setText("Login successful!");

            // Load chat window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/welcome.fxml"));
            Parent chatRoot = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Chat Window");
            stage.setScene(new Scene(chatRoot));
            stage.show();

        } else if (user.isEmpty() || pass.isEmpty()) {
            wronglogin.setText("Please enter your data");
        } else {
            wronglogin.setText("Wrong username or password");
        }

    }


}
