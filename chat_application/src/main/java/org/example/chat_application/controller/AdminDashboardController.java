package org.example.chat_application.controller;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private AnchorPane Welcome;

    @FXML
    private AnchorPane ManageUser;

    @FXML
    private AnchorPane Notification;

    @FXML
    private AnchorPane Setting;

    @FXML
    private AnchorPane creatchat;

    @FXML
    private Button NewChatBtn;

    //Notification
    @FXML
    private VBox notificationsContainer;

    @FXML
    private Button showallBtn;

    @FXML
    void handleShowAll(ActionEvent event) {

    }
    public void addNotification(String username, String message, String timeAgo) {
        HBox notificationBox = new HBox(10);
        notificationBox.setStyle("-fx-alignment: center-left;");

        VBox textBox = new VBox(5);

        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label messageLabel = new Label(message + " • " + timeAgo);
        messageLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

        textBox.getChildren().addAll(nameLabel, messageLabel);
        notificationBox.getChildren().add(textBox);

        notificationsContainer.getChildren().add(notificationBox);
    }

    @FXML
    void create_chat(ActionEvent event) {
        creatchat.setVisible(true);
        ManageUser.setVisible(false);
        Notification.setVisible(false);
        Setting.setVisible(false);
        Welcome.setVisible(false);
    }

    @FXML
    void manage_user(ActionEvent event) {
        creatchat.setVisible(false);
        ManageUser.setVisible(true);
        Notification.setVisible(false);
        Setting.setVisible(false);
        Welcome.setVisible(false);
    }

    @FXML
    void notification(ActionEvent event) {
        Notification.setVisible(true);
        ManageUser.setVisible(false);
        creatchat.setVisible(false);
        Setting.setVisible(false);
        Welcome.setVisible(false);
    }

    @FXML
    void setting(ActionEvent event) {
        Setting.setVisible(true);
        Notification.setVisible(false);
        ManageUser.setVisible(false);
        creatchat.setVisible(false);
        Welcome.setVisible(false);
    }

    @FXML
    void Create_Chat(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/Chat_Form.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("New Chat Form");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



