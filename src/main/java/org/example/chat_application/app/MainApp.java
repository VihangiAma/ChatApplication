package org.example.chat_application.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/chat_application/view/welcome.fxml")));
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
